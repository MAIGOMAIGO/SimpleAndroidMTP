# AndroidAPIのMTP備忘録
情報が落ちてなくて面倒だったので忘れた時用に作ってみた

# 参照
| 名称                    | URL                                                                                                                                 |
|:----------------------|:------------------------------------------------------------------------------------------------------------------------------------|
| 概要                    | [android.mtp \| API reference \| Android Developers](https://developer.android.com/reference/android/mtp/package-summary)           |
| MtpConstants          | [MtpConstants \| API reference \| Android Developers](https://developer.android.com/reference/android/mtp/MtpConstants)             |
| MtpDevice             | [MtpDevice \| API reference \| Android Developers](https://developer.android.com/reference/android/mtp/MtpDevice)             |
| MtpDeviceInfo         | [MtpDeviceInfo \| API reference \| Android Developers](https://developer.android.com/reference/android/mtp/MtpDeviceInfo)         |
| MtpEvent              | [MtpEvent \| API reference \| Android Developers](https://developer.android.com/reference/android/mtp/MtpEvent)              |
| MtpObjectInfo         | [MtpObjectInfo \| API reference \| Android Developers](https://developer.android.com/reference/android/mtp/MtpObjectInfo)         |
| MtpObjectInfo.Builder | [MtpObjectInfo.Builder \| API reference \| Android Developers](https://developer.android.com/reference/android/mtp/MtpObjectInfo.Builder) |
| MtpStorageInfo        | [MtpStorageInfo \| API reference \| Android Developers](https://developer.android.com/reference/android/mtp/MtpStorageInfo)        |

# MTPとは  
Media Transfer Protocol (MTP) とのことで、動画像などの転送用らしいです。  
AndroidをPCに繋いだ時にストレージを閲覧するのが出来るのがこれのおかげっぽいです。  

# 用語  
## PTP（Picture Transfer Protocol）  
デジタルカメラやスマホをUSB接続し、画像をPCへ転送する標準規格とのこと。  
ここら辺の規格利用してMTPを作ったとか。  

## DeviceId  
USBデバイスのIDです。
接続中の端末に個別に割り振るっぽいのでハブとかで複数繋いでも判別できるみたいです。  
USBインターフェース側の仕様なのでMTPでは利用するだけ。  

## storageId  
ストレージに割り振られるID  

## objectHandle  
MTPデバイス内のファイルやフォルダに割り振られる番号  
指定のファイルやフォルダに操作を加えたい時に使用  

