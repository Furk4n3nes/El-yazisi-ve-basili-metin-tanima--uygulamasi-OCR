## Gemini Yazı Tanıma (Android)

Android için geliştirilmiş el yazısı ve basılı metin tanıma (OCR) uygulaması. Kamera veya galeriden alınan görüntülerdeki metinleri dijital metne dönüştürmeyi, düzenlemeyi ve paylaşmayı kolaylaştırır. Proje raporunda belirtildiği üzere, metin işleme akışında Google Gemini ve/veya Google ML Kit gibi yapay zeka tabanlı servislerden yararlanacak şekilde tasarlanmıştır.

### Özellikler
- **OCR**: El yazısı ve basılı metinlerden metin çıkarma
- **Giriş kaynakları**: Kamera ile tarama veya galeriden görsel seçme
- **İşlemler**: Metni düzenleme, kopyalama, kaydetme ve paylaşma
- **Arayüz**: Material tasarım prensiplerine uygun, modern Android arayüzü

### Gereksinimler
- **Android Studio**: Hedgehog/Koala ve üzeri önerilir
- **JDK**: 17 önerilir
- **Minimum API**: Android 8.0 (API 26) ve üzeri cihaz/emülatör
- **Gradle/AGP**: Proje ile gelen sürümler

### Kurulum
1. Depoyu indirin veya klonlayın.
2. Android Studio ile `geminiyazitanima` dizinini açın.
3. İlk açılışta bağımlılıklar otomatik indirilecektir.

### Yapılandırma (Opsiyonel: API Anahtarı)
Uygulama, raporda belirtildiği şekilde Google Gemini API kullanacak şekilde düzenlenmiş olabilir. API anahtarınızı güvenle sağlamak için:
1. Proje kökündeki `local.properties` dosyasına aşağıdaki satırı ekleyin (VCS'e dahil edilmez):
```
GEMINI_API_KEY=YOUR_API_KEY
```
2. Anahtarı `build.gradle.kts` üzerinden `BuildConfig` veya `manifestPlaceholders` ile uygulamaya aktarın. Düz metin olarak kaynak koduna koymayın.

Not: Yalnızca Google ML Kit ile cihaz üstü OCR kullanılıyorsa API anahtarı gerekmeyebilir. En doğru bilgi için kod içindeki kullanım noktalarına bakın.

### Çalıştırma
1. Bir cihaz bağlayın veya emülatör başlatın.
2. Android Studio'da Run ▶ ile uygulamayı çalıştırın.
3. İlk çalıştırmada kamera/depolama izinlerini verin.
4. Kamera ile belgeyi tarayın veya galeriden görsel seçin.
5. Tanınan metni düzenleyin, kaydedin veya paylaşın.

### Proje Yapısı (Özet)
- `app/` — Android uygulama modülü (kaynak kodu, kaynaklar, manifest)
- `build.gradle.kts`, `settings.gradle.kts` — Gradle yapılandırmaları
- `docs/`, `rapor.txt` — Proje açıklama ve analiz dokümanları

### Sorun Giderme
- Derleme hatalarında AGP/Gradle ve JDK 17 kurulumunu kontrol edin.
- Kamera/galeri erişimi için çalışma zamanı izinlerinin verildiğinden emin olun.
- Uzak API kullanıyorsanız `GEMINI_API_KEY` değerinin tanımlı olduğundan emin olun.

### Katkıda Bulunma
- Hata/öneri için issue açın.
- Değişikliklerde fork ➜ branch ➜ Pull Request akışını izleyin.

### Lisans
Proje için ayrı bir lisans dosyası belirtilmemiştir. Kullanım koşulları için proje sahibiyle iletişime geçin.
