if [ -z "${BOT_TOKEN}" ]; then
  exit 0
fi

root_dir="$(pwd)"
packaged_dir="${root_dir}/packaged"
mkdir -p "$packaged_dir"
for artifact in artifacts/*; do
  if [ -d "$artifact" ]; then
    file_count=$(find "$artifact" -type f | wc -l | tr -d ' ')
    dir_count=$(find "$artifact" -type d | wc -l | tr -d ' ')
    if [ "$file_count" -eq 1 ] && [ "$dir_count" -eq 1 ]; then
      file=$(find "$artifact" -type f | head -n 1)
      cp "$file" "${packaged_dir}/$(basename "$file")"
    else
      name=$(basename "$artifact")
      (cd "$artifact" && zip -qr "${packaged_dir}/${name}.zip" .)
    fi
  elif [ -f "$artifact" ]; then
    cp "$artifact" "${packaged_dir}/$(basename "$artifact")"
  fi
done

python3 - <<'PY'
import json
import os

text = os.environ.get("COMMIT_MESSAGE", "")
max_len = 1024
if len(text) > max_len:
    text = text[: max_len - 3] + "..."
escape_chars = r"_*[]()~`>#+-=|{}.!\\"
escaped = []
for ch in text:
    if ch in escape_chars:
        escaped.append("\\" + ch)
    else:
        escaped.append(ch)
text = "".join(escaped)

max_batch_files = int(os.environ.get("TELEGRAM_MAX_BATCH_FILES", "10"))
max_batch_bytes = int(os.environ.get("TELEGRAM_MAX_BATCH_BYTES", str(45 * 1024 * 1024)))
max_file_bytes = int(os.environ.get("TELEGRAM_MAX_FILE_BYTES", str(200 * 1024 * 1024)))

files = []
for name in sorted(os.listdir("packaged")):
    path = os.path.join("packaged", name)
    if os.path.isfile(path):
        files.append((name, os.path.getsize(path)))

batches = []
singles = []
oversized = []
current = []
current_bytes = 0
for name, size in files:
    if size > max_file_bytes:
        oversized.append(name)
        continue
    if size > max_batch_bytes:
        singles.append((name, size))
        continue
    if current and (len(current) >= max_batch_files or current_bytes + size > max_batch_bytes):
        batches.append(current)
        current = []
        current_bytes = 0
    if not current and size > max_batch_bytes:
        batches.append([(name, size)])
        continue
    current.append((name, size))
    current_bytes += size
if current:
    batches.append(current)

os.makedirs("telegram_batches", exist_ok=True)
with open("caption.txt", "w", encoding="utf-8") as f:
    f.write(text)
with open("batches.list", "w", encoding="utf-8") as f:
    for i, batch in enumerate(batches):
        media = []
        for index, (name, _) in enumerate(batch):
            item = {"type": "document", "media": f"attach://file{index}"}
            if i == 0 and index == 0 and text:
                item["caption"] = text
                item["parse_mode"] = "MarkdownV2"
            media.append(item)
        media_path = os.path.join("telegram_batches", f"media_{i}.json")
        files_path = os.path.join("telegram_batches", f"files_{i}.txt")
        with open(media_path, "w", encoding="utf-8") as mf:
            json.dump(media, mf)
        with open(files_path, "w", encoding="utf-8") as ff:
            ff.write("\n".join([name for name, _ in batch]))
        f.write(f"{media_path}|{files_path}\n")
with open("singles.list", "w", encoding="utf-8") as f:
    for name, _ in singles:
        f.write(name + "\n")

if oversized:
    with open("oversized.txt", "w", encoding="utf-8") as f:
        f.write("\n".join(oversized))
PY

caption_text=$(cat caption.txt)
batches_count=$(wc -l <batches.list | tr -d ' ')
singles_count=$(wc -l <singles.list | tr -d ' ')
if [ "$batches_count" -eq 0 ] && [ "$singles_count" -eq 0 ]; then
  exit 0
fi

if [ "$batches_count" -gt 0 ]; then
  while IFS= read -r line; do
    [ -z "$line" ] && continue
    media_path="${line%%|*}"
    files_path="${line##*|}"
    args=()
    index=0
    while IFS= read -r name; do
      if [ -n "$name" ]; then
        args+=(-F "file${index}=@packaged/${name}")
        index=$((index + 1))
      fi
    done <"$files_path"
    curl -s "https://api.telegram.org/bot${BOT_TOKEN}/sendMediaGroup" \
      -F "chat_id=${CHANNEL_ID}" \
      -F "media=$(cat "$media_path")" \
      "${args[@]}"
  done <batches.list
fi

if [ "$singles_count" -gt 0 ]; then
  first_single=1
  while IFS= read -r name; do
    [ -z "$name" ] && continue
    if [ "$batches_count" -eq 0 ] && [ -n "$caption_text" ] && [ "$first_single" -eq 1 ]; then
      curl -s "https://api.telegram.org/bot${BOT_TOKEN}/sendDocument" \
        -F "chat_id=${CHANNEL_ID}" \
        -F "document=@packaged/${name}" \
        -F "caption=${caption_text}" \
        -F "parse_mode=MarkdownV2"
      first_single=0
    else
      curl -s "https://api.telegram.org/bot${BOT_TOKEN}/sendDocument" \
        -F "chat_id=${CHANNEL_ID}" \
        -F "document=@packaged/${name}"
    fi
  done <singles.list
fi

if [ -f oversized.txt ]; then
  oversized_text=$(python3 - <<'PY'
import os
with open("oversized.txt", "r", encoding="utf-8") as f:
    names = [line.strip() for line in f if line.strip()]
if names:
    print("Skipped oversized files:\n" + "\n".join(names))
PY
)
  if [ -n "$oversized_text" ]; then
    curl -s "https://api.telegram.org/bot${BOT_TOKEN}/sendMessage" \
      -d "chat_id=${CHANNEL_ID}" \
      --data-urlencode "text=${oversized_text}" \
      > /dev/null
  fi
fi
