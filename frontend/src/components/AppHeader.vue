<script setup>
import { computed } from 'vue'

import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import { t } from '@/i18n'

const props = defineProps({
  email: {
    type: String,
    default: '',
  },
})

const userInitial = computed(() => props.email.trim().charAt(0).toUpperCase() || '?')
</script>

<template>
  <header class="app-header">
    <RouterLink class="brand" to="/dashboard">
      <span class="brand-name">AmtPilot</span>
      <span class="brand-tagline">{{ t('header.tagline') }}</span>
    </RouterLink>

    <div class="header-actions">
      <LanguageSwitcher />

      <RouterLink
        class="settings-link"
        to="/settings"
        :aria-label="t('header.settings')"
        :title="t('settings.title')"
      >
        {{ userInitial }}
      </RouterLink>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #ffffff;
}

.brand {
  display: grid;
  min-width: 0;
  gap: 0.15rem;
  text-decoration: none;
}

.brand-name {
  color: #2563eb;
  font-size: 20px;
  font-weight: 800;
  line-height: 1.1;
}

.brand-tagline {
  color: #64748b;
  font-size: 0.78rem;
  font-weight: 500;
  line-height: 1.3;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.settings-link {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border: 1px solid #bfdbfe;
  border-radius: 50%;
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 700;
  text-decoration: none;
  transition:
    background 150ms ease,
    transform 150ms ease;
}

.settings-link:hover,
.settings-link:focus-visible {
  background: #dbeafe;
  transform: translateY(-1px);
}

.settings-link:focus-visible {
  outline: 3px solid rgb(37 99 235 / 18%);
}

@media (max-width: 600px) {
  .app-header {
    gap: 1rem;
    padding: 16px 20px;
  }

  .brand-tagline {
    max-width: 12rem;
  }
}
</style>
