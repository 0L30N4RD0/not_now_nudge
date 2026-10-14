# Context Nudge 0.3 — código-fonte

Aplicativo independente para Android, sem root. Processa localmente notificações após consentimento explícito; não salva texto de notificações. Sugestões nunca criam eventos sem confirmação.

## Novidades
- Datas: hoje, amanhã, depois de amanhã, `19 de dezembro`, `19/12`, `19/12/2027`, dias da semana e horário explícito.
- Títulos básicos por contexto (prova, consulta, reunião, aula, entrevista).
- Janela flutuante experimental, opcional, requer autorização `Aparecer sobre outros apps`; botões Ignorar e Revisar.
- Notificação normal permanece disponível.

## Limites
- Não há integração oficial com Now Bar/Now Brief: APIs e permissões da Samsung não são públicas para esse caso. Não imitamos uma sessão de mídia para forçar presença na Now Bar.
- Não copia código ou modelos da Samsung. Arquitetura inspirada em análise funcional independente.
- Parser conservador: exige data futura e hora explícita; não infere duração. Próxima ocorrência de dia da semana é escolhida (hoje aponta para semana seguinte).
- Sobreposição pode não aparecer quando Android/Samsung restringir janelas ou app estiver em tela sensível.
- A janela flutuante é experimental; se houver incômodo, desative no aplicativo ou revogue a permissão do Android.

## Build
Workflow GitHub Actions incluso: execute Build test APK, baixe artefato `ContextNudge-debug-apk`. Debug builds em runners distintos podem usar assinaturas diferentes; se não atualizar, desinstale a versão anterior (isso apaga preferências).

## Teste
Ative análise, autorize notificações, opcionalmente ative sobreposição. Use Termux:API `termux-notification --id 9876 --title "Teste" --content "Tenho prova dia 19 de dezembro às 14h"`. A data deve ser futura em relação ao dia do teste. Revise no calendário, sem salvar automaticamente.
