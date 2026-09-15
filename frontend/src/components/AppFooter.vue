<script setup>
import { t } from '@/i18n'
</script>

<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div>
        <RouterLink class="footer-brand" to="/">AmtPilot</RouterLink>
        <p>{{ t('footer.developedBy') }}</p>
      </div>

      <nav :aria-label="t('footer.about')">
        <RouterLink to="/about">{{ t('footer.about') }}</RouterLink>
        <RouterLink to="/about#how-it-works">{{ t('footer.howItWorks') }}</RouterLink>
        <a href="https://github.com/saeed-mh/amt-pilot" target="_blank" rel="noopener noreferrer">
          {{ t('footer.github') }}
        </a>
      </nav>
    </div>

    <div class="footer-bottom">{{ t('footer.copyright') }}</div>
  </footer>
</template>

<style scoped>
.app-footer {
  padding: 28px 24px 20px;
  border-top: 1px solid #dbe3ef;
  background: #ffffff;
  color: #64748b;
}

.footer-content,
.footer-bottom {
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.footer-brand {
  color: #2563eb;
  font-size: 18px;
  font-weight: 700;
  text-decoration: none;
}

.footer-content p {
  margin: 7px 0 0;
  font-size: 13px;
}

nav {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 18px;
}

nav a {
  color: #475569;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
}

nav a:hover {
  color: #2563eb;
}

.footer-bottom {
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid #edf1f7;
  font-size: 12px;
}

@media (max-width: 600px) {
  .footer-content {
    align-items: flex-start;
    flex-direction: column;
  }

  nav {
    justify-content: flex-start;
  }
}
</style>
