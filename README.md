# Context Nudge 0.1 — protótipo de teste

Aplicativo independente em Kotlin. Não contém código nem recursos proprietários da Samsung. Processa texto localmente e abre a tela de criação de evento do calendário para revisão, sem salvar automaticamente.

## Como obter um APK usando GitHub Actions (sem instalar Android Studio)

1. Crie um repositório GitHub **privado** e envie o conteúdo desta pasta para a raiz do repositório, incluindo `.github/workflows/build-apk.yml`.
2. Abra **Actions** > **Build test APK** > **Run workflow**. Se a interface pedir, habilite Actions no repositório.
3. Aguarde a execução ficar verde. Abra a execução e, em **Artifacts**, baixe `ContextNudge-debug-apk`.
4. Extraia o ZIP baixado: dentro dele estará `app-debug.apk`. Transfira para o S23 e instale-o. O Android pode solicitar autorização para instalar aplicativos desta origem.
5. Abra **Context Nudge**, digite `Amanhã às 15h vamos ao shopping`, toque em **Analisar texto** e depois **Revisar no calendário**.

O arquivo ZIP do projeto **não é um APK**. A compilação ainda não foi executada neste ambiente. Não compartilhe o APK com terceiros como se fosse uma versão final.

## Compilação local alternativa

Com Android Studio, JDK 17 e SDK 35, importe o projeto e execute `:app:testDebugUnitTest :app:assembleDebug` (Gradle 8.9). APK em `app/build/outputs/apk/debug/app-debug.apk`.

## Limitações

- Suporta hoje, amanhã, depois de amanhã e horário explícito em português.
- Não monitora conversas, tela ou notificações; recebe texto manual ou via compartilhamento `text/plain`.
- Requer aplicativo de calendário instalado para abrir a sugestão.
- Nenhuma permissão sensível, internet ou root.
