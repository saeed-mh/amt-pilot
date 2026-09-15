<script setup>
import { locale, setLocale, t } from '@/i18n'
</script>

<template>
  <div class="language-switcher" role="group" :aria-label="t('language.label')">
    <button
      type="button"
      :class="{ active: locale === 'en' }"
      :aria-pressed="locale === 'en'"
      :title="t('language.english')"
      @click="setLocale('en')"
    >
      EN
    </button>
    <button
      type="button"
      :class="{ active: locale === 'de' }"
      :aria-pressed="locale === 'de'"
      :title="t('language.german')"
      @click="setLocale('de')"
    >
      DE
    </button>
  </div>
</template>

<style scoped>
.language-switcher {
  display: inline-flex;
  padding: 3px;
  border: 1px solid #dbe3ef;
  border-radius: 9px;
  background: #f8fafc;
}

button {
  min-width: 38px;
  padding: 7px 9px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #64748b;
  font: inherit;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

button:hover {
  color: #1d4ed8;
}

button.active {
  background: #ffffff;
  box-shadow: 0 1px 3px rgb(15 23 42 / 12%);
  color: #1d4ed8;
}

button:focus-visible {
  outline: 2px solid #2563eb;
  outline-offset: 1px;
}
</style>
