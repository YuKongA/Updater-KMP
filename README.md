# Updater-KMP
This is an application that gets MIUI/HyperOS ROM information. With the [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) UI framework. **Android** / **Desktop(JVM)** / **iOS** are fully supported. **Web(Js/WasmJs)** is also basically supported.

<div style="position:relative; display: flex; flex-wrap: nowrap;"> 
    <img style='position:absolute; z-index:1;' src='https://github.com/YuKongA/Updater-KMP/blob/main/screenshot/screenshot.jpg?raw=true'/>
</div>

## Usage:
When obtaining the `Pubilc Release Version (F)`, the suffix of the system version can be automatically completed using `AUTO`<br />For example: `OS1.0.26.0.AUTO` / `V14.0.4.0.AUTO`

When obtaining the `Beta Development Version (X)`, please enter the complete system version<br />For example: `OS1.0.23.12.19.DEV` / `V14.0.23.5.8.DEV`

## Notes:
Only supported `MIUI9` and above versions. The most extreme case is: Redmi 1S (armani), MIUI9, Android4.4.

Only devices in the list of [DeviceInfoHelper](https://github.com/YuKongA/Updater-KMP/blob/main/composeApp/src/commonMain/kotlin/data/DeviceInfoHelper.kt#L28) are supported use `AUTO` to complete automatically, other devices still need to manually enter the full system version.

When you are not logged in with a Xiaomi account, you can use the miotaV3-v1 interface to obtain any detailed information of the `Pubilc Release Version` of any model.

After logging in to your Xiaomi account, you will use the miotaV3-v2 interface to obtain detailed information about the `Beta Release Version` or the `Public Development Version`, corresponding to the internal test permissions you have.

## Credits:
- [compose-imageloader](https://github.com/qdsfdhvh/compose-imageloader) with MIT License
- [compose-multiplatform](https://github.com/JetBrains/compose-multiplatform) with Apache-2.0 license
- [cryptography-kotlin](https://github.com/whyoleg/cryptography-kotlin) with Apache-2.0 license
- [haze](https://github.com/chrisbanes/haze) with Apache-2.0 license
- [ktor](https://github.com/ktorio/ktor) with Apache-2.0 license
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) with Apache-2.0 license
- [miuix](https://github.com/miuix-kotlin-multiplatform/miuix) with Apache-2.0 license
