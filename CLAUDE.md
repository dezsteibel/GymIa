# GymAI — Claude Code Instructions

## Workflow obrigatório
- NUNCA faça commit direto na branch main
- Para cada feature, crie uma branch: feature/nome-da-feature
- Ao terminar uma feature, faça commit + push da branch
- Aguarde instrução do usuário antes de iniciar a próxima feature

## Padrão de branches
feature/room-setup
feature/workout-recording
feature/history-screen
feature/progress-charts
feature/cardio-recording
feature/ai-integration

## Padrão de commits (Conventional Commits)
feat: adiciona tela de registro de treino
fix: corrige cálculo de volume semanal
refactor: extrai lógica de série para use case
chore: adiciona dependências do Room

## Arquitetura — Clean Architecture + DDD
- ui/ → apenas Composables e ViewModels. Zero lógica de negócio.
- domain/ → Use Cases e modelos de domínio. Zero dependência de Android.
- data/ → Repositories, DAOs, DTOs. Zero lógica de negócio.
- Regra de dependência: ui → domain ← data

## Clean Code — regras inegociáveis
- Funções com no máximo 20 linhas
- Uma responsabilidade por classe
- Nomes descritivos em inglês (sem abreviações)
- Sem comentários óbvios — o código deve se auto-documentar
- Sem números mágicos — use constantes nomeadas
- Composables sem lógica — apenas recebem estado e emitem eventos

## Antes de cada commit, verifique
- O código compila sem warnings?
- Os Use Cases estão no domain/, não no ViewModel?
- O ViewModel expõe apenas StateFlow<UiState>?
- Nenhuma referência ao Android framework no domain/?

## Leia sempre
Consulte SPEC.md para decisões técnicas, dependências e estrutura de pastas.