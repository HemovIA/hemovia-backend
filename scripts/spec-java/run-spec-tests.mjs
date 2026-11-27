if (process.env.HEMOVIA_OBS_VERIFY === 'true') {
  await import('../../tests/observabilidade/stack.test.mjs');
  await import('../../tests/observabilidade/entrega.test.mjs');
} else {
  await import('./verify-tests.mjs');
}
