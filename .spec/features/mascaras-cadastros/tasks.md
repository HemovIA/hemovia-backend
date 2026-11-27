# Tasks: Máscaras de CPF e telefone

> feature: mascaras-cadastros

Execução sequencial com as preferências já confirmadas do usuário.

## T-053 — Implementar máscaras e provar interação dos formulários [concluida]
- Refs: US-036, AC-109, AC-110, AC-111, AC-112, AC-113
- Arquivos: ../hemovia-frontend/src/masks.tsx, ../hemovia-frontend/src/registries.tsx, ../hemovia-frontend/src/donor.tsx, ../hemovia-frontend/tests/masks.spec.ts, ../hemovia-frontend/README.md
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Ajustar mapa aos arquivos reais, preservar controles e dados existentes, normalizar somente os payloads dos campos envolvidos; provar digitação, colagem, edição, limites e bloqueios em testes reais de interface. Um commit local seletivo no frontend.

## T-054 — Verificar e registrar as provas [concluida]
- Refs: US-036, AC-109, AC-110, AC-111, AC-112, AC-113
- Arquivos: scripts/spec-java/verify-tests.mjs, scripts/spec-java/test-formularios.ps1, .spec/features/mascaras-cadastros/resultado-validacao.md, .spec/features/mascaras-cadastros/andamento.md, .spec/features/mascaras-cadastros/mascaras-cadastros.png
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Integrar tags e resultados reais ao runner sem alterar a skill nem enfraquecer o gate; atualizar mapa antes do congelamento. Build e testes específicos, depois verify da feature e renovação real das anteriores no harness isolado, seguido de auditoria exit 0. Conferir também exitCode dos arquivos de prova; concluir tarefas somente com PASS.
