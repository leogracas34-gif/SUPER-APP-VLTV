# 📺 VLTV Plus

<p align="center">
  <img src="app/src/main/res/drawable/ic_logo_vltv.xml" width="200"/>
</p>

<p align="center">
  <b>Aplicativo IPTV moderno, robusto e inteligente</b><br/>
  Compatível com Android, Android TV, Fire Stick, Mi Box e TV Box
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-5.0%2B-green?logo=android"/>
  <img src="https://img.shields.io/badge/Android%20TV-supported-blue?logo=androidtv"/>
  <img src="https://img.shields.io/badge/Kotlin-1.9.22-purple?logo=kotlin"/>
  <img src="https://img.shields.io/badge/ExoPlayer-Media3-orange"/>
  <img src="https://img.shields.io/badge/Build-GitHub%20Actions-black?logo=githubactions"/>
</p>

---

## ✨ Funcionalidades

| Funcionalidade | Detalhe |
|---|---|
| 🌐 **MultiDNS Inteligente** | Testa 15 servidores em paralelo e conecta no mais rápido |
| 📺 **TV ao Vivo** | Lista de canais por categoria, mini preview, EPG (grade de programação) |
| 🎬 **Filmes** | Grid com categorias na lateral, imagens TMDB, detalhes completos |
| 📺 **Séries** | Temporadas, episódios com thumbnail, sinopse por episódio |
| 🔍 **Busca Global** | Pesquisa filmes, séries e canais em tempo real com debounce |
| ▶️ **Player ExoPlayer Media3** | HLS, DASH, MP4, TS — o mais moderno disponível |
| ⏭️ **Pular Abertura** | Botão aparece nos primeiros 90 segundos |
| ➡️ **Próximo Episódio** | Aparece 60 segundos antes do fim do episódio |
| ⏸️ **Continuar Assistindo** | Salva posição automaticamente a cada 5 segundos |
| 🖼️ **TMDB Integrado** | Capas, backdrops, trailers, elenco e títulos semelhantes |
| 📱 **Modo Retrato/Paisagem** | Celular em retrato, TV em paisagem — automático |
| 🎮 **Controle Remoto** | D-Pad, Play/Pause, Rewind, Forward, Back — tudo mapeado |
| 📌 **Favoritos** | Salva filmes, séries e canais favoritos offline |
| 🔄 **Sync Automático** | Atualiza conteúdo a cada 3 horas em background |
| 📦 **Banco de Dados** | Room com WAL mode — ultra rápido |
| 📺 **PiP** | Picture in Picture no Android 8+ |

---

## 🏗️ Arquitetura

```
MVVM + Clean Architecture + Repository Pattern

UI Layer     → Activities, Fragments, Adapters
ViewModel    → StateFlow, SharedFlow, LiveData
Repository   → Única fonte de verdade
Data Layer   → Room (local) + Retrofit (remoto)
DI           → Hilt (injeção de dependência)
```

---

## 🌐 Servidores DNS

O app testa automaticamente todos os 15 servidores e conecta no mais rápido:

```
fibercdn.sbs · tvblack.shop · blackzz.shop · playchannels.shop
xppv.shop · redeinternadestiny.top · blackstartv.shop · blackdns.shop
ranos.sbs · cmdtv.casa · cmdtv.pro · cmdtv.sbs · cmdtv.top
cmdbr.life · blackdeluxe.shop
```

---

## 📱 Compatibilidade

| Dispositivo | Suporte |
|---|---|
| Android Phone/Tablet | ✅ API 21+ (Android 5.0) |
| Android TV | ✅ Leanback + D-Pad |
| Amazon Fire Stick / Fire TV | ✅ |
| Xiaomi Mi Box / Mi Stick | ✅ |
| TV Box (Android) | ✅ |
| Chromecast with Google TV | ✅ |

---

## 🛠️ Stack Tecnológica

| Tecnologia | Versão | Uso |
|---|---|---|
| Kotlin | 1.9.22 | Linguagem principal |
| ExoPlayer Media3 | 1.3.1 | Player de vídeo |
| Hilt | 2.50 | Injeção de dependência |
| Room | 2.6.1 | Banco de dados local |
| Retrofit | 2.9.0 | Requisições HTTP |
| OkHttp | 4.12.0 | Cliente HTTP |
| Glide | 4.16.0 | Carregamento de imagens |
| Navigation Component | 2.7.7 | Navegação entre telas |
| Coroutines | 1.7.3 | Programação assíncrona |
| DataStore | 1.0.0 | Preferências persistentes |
| WorkManager | 2.9.0 | Sync em background |
| Palette | 1.0.0 | Extração de cores das capas |
| Shimmer | 0.5.0 | Efeito de carregamento |

---

## 🚀 Como subir para o GitHub e compilar

### 1. Criar repositório
```
1. Acesse github.com → New Repository
2. Nome: VLTVPlus
3. Visibilidade: Private (recomendado)
4. Clique em "Create repository"
```

### 2. Subir os arquivos
```
1. Na página do repositório, clique em "uploading an existing file"
2. Arraste e solte TODOS os arquivos do ZIP mantendo a estrutura de pastas
   (ou use "Add file → Upload files")
3. Commit: "Initial commit - VLTV Plus project"
4. Clique em "Commit changes"
```

### 3. Compilar automaticamente
```
1. Vá em Actions (aba superior do repositório)
2. O build inicia automaticamente ao fazer push
3. Aguarde ~5 minutos
4. Em "Artifacts" baixe o APK gerado
```

### 4. Build manual
```
1. Actions → "Build VLTV Plus APK" → "Run workflow"
2. Escolha: debug ou release
3. Clique em "Run workflow"
4. Baixe o APK em Artifacts
```

---

## 📂 Estrutura de Arquivos

```
VLTV_Plus/
├── .github/
│   └── workflows/
│       └── build.yml                    ← CI/CD automático
├── build.gradle                         ← Configuração root
├── settings.gradle                      ← Módulos do projeto
├── gradle.properties                    ← Propriedades Gradle
└── app/
    ├── build.gradle                     ← Dependências e configs
    ├── proguard-rules.pro               ← Regras de ofuscação
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/vltvplus/
        │   ├── VLTVApplication.kt
        │   ├── data/
        │   │   ├── api/
        │   │   │   ├── DnsManager.kt         ← MultiDNS
        │   │   │   ├── XtreamApiService.kt   ← API IPTV
        │   │   │   └── TmdbApiService.kt     ← API TMDB
        │   │   ├── database/
        │   │   │   ├── VLTVDatabase.kt
        │   │   │   ├── dao/Daos.kt           ← 9 DAOs
        │   │   │   └── entities/Entities.kt  ← 9 Tabelas
        │   │   ├── models/Models.kt          ← Todos os modelos
        │   │   └── repository/
        │   │       └── VLTVRepository.kt     ← Repositório central
        │   ├── di/
        │   │   └── AppModule.kt              ← Injeção Hilt
        │   ├── service/
        │   │   ├── SyncService.kt            ← WorkManager sync
        │   │   └── PlaybackService.kt        ← Media3 session
        │   ├── ui/
        │   │   ├── auth/
        │   │   │   ├── SplashActivity.kt
        │   │   │   ├── LoginActivity.kt
        │   │   │   └── AuthViewModel.kt
        │   │   ├── home/
        │   │   │   ├── MainActivity.kt
        │   │   │   ├── HomeFragment.kt
        │   │   │   ├── HomeViewModel.kt
        │   │   │   └── adapters/HomeAdapters.kt
        │   │   ├── live/
        │   │   │   ├── LiveTvFragment.kt
        │   │   │   ├── LiveTvViewModel.kt
        │   │   │   └── adapters/LiveAdapters.kt
        │   │   ├── movies/
        │   │   │   ├── MoviesFragment.kt
        │   │   │   ├── MoviesViewModel.kt
        │   │   │   └── adapters/MovieAdapters.kt
        │   │   ├── series/
        │   │   │   ├── SeriesFragment.kt
        │   │   │   ├── SeriesViewModel.kt
        │   │   │   └── adapters/SeriesAdapters.kt
        │   │   ├── search/
        │   │   │   ├── SearchFragment.kt
        │   │   │   ├── SearchViewModel.kt
        │   │   │   └── adapters/SearchResultAdapter.kt
        │   │   ├── detail/
        │   │   │   ├── MovieDetailActivity.kt
        │   │   │   ├── MovieDetailViewModel.kt
        │   │   │   ├── SeriesDetailActivity.kt
        │   │   │   ├── SeriesDetailViewModel.kt
        │   │   │   └── adapters/DetailAdapters.kt
        │   │   └── player/
        │   │       ├── PlayerActivity.kt     ← ExoPlayer Media3 completo
        │   │       └── PlayerViewModel.kt
        │   └── utils/
        │       ├── DeviceUtils.kt            ← Detecta TV/Celular
        │       ├── RemoteKeyUtils.kt         ← Controle remoto
        │       ├── PreferenceManager.kt      ← DataStore
        │       ├── Resource.kt
        │       └── extensions/Extensions.kt
        └── res/
            ├── layout/          ← 16 layouts XML
            ├── drawable/        ← 30+ ícones e backgrounds
            ├── color/           ← 3 color selectors
            ├── values/          ← colors, strings, themes, dimens
            ├── anim/            ← 8 animações
            ├── menu/            ← bottom_nav_menu
            └── navigation/      ← nav_graph
```

---

## ⚙️ Configuração da Chave de Release (opcional)

Para assinar o APK em produção, adicione os Secrets no GitHub:

```
Settings → Secrets → Actions → New repository secret

KEYSTORE_BASE64  → keystore em base64 (cat keystore.jks | base64)
KEY_ALIAS        → alias da sua chave
KEY_PASSWORD     → senha da chave
STORE_PASSWORD   → senha do keystore
```

---

## 📄 Licença

Projeto privado — todos os direitos reservados © 2025 VLTV Plus
