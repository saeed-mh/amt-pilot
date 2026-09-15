<script setup>
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import { t } from '@/i18n'

const isAuthenticated = Boolean(localStorage.getItem('amtpilot_access_token'))
</script>

<template>
  <main class="about-page">
    <section class="about-layout">
      <header class="about-header">
        <RouterLink class="brand" to="/">AmtPilot</RouterLink>

        <div class="header-actions">
          <LanguageSwitcher />
          <RouterLink class="app-link" :to="isAuthenticated ? '/dashboard' : '/login'">
            {{ isAuthenticated ? t('about.openDashboard') : t('about.login') }}
          </RouterLink>
        </div>
      </header>

      <section class="hero-card">
        <p class="eyebrow">{{ t('about.eyebrow') }}</p>
        <h1>{{ t('about.title') }}</h1>
        <p>{{ t('about.description') }}</p>
      </section>

      <section class="purpose-card">
        <h2>{{ t('about.purposeTitle') }}</h2>
        <p>{{ t('about.purposeDescription') }}</p>
      </section>

      <section id="how-it-works" class="how-card">
        <h2>{{ t('about.howTitle') }}</h2>

        <ol>
          <li>
            <span>1</span>
            <div>
              <h3>{{ t('about.stepOneTitle') }}</h3>
              <p>{{ t('about.stepOneDescription') }}</p>
            </div>
          </li>
          <li>
            <span>2</span>
            <div>
              <h3>{{ t('about.stepTwoTitle') }}</h3>
              <p>{{ t('about.stepTwoDescription') }}</p>
            </div>
          </li>
          <li>
            <span>3</span>
            <div>
              <h3>{{ t('about.stepThreeTitle') }}</h3>
              <p>{{ t('about.stepThreeDescription') }}</p>
            </div>
          </li>
        </ol>
      </section>

      <section class="portfolio-card">
        <h2>{{ t('about.portfolioTitle') }}</h2>
        <p>{{ t('about.portfolioDescription') }}</p>
      </section>
    </section>
  </main>
</template>

<style scoped>
.about-page {
  min-height: 100vh;
  padding: 24px;
  background: #f4f7fb;
  color: #1f2937;
}

.about-layout {
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
}

.about-header,
.header-actions {
  display: flex;
  align-items: center;
}

.about-header {
  justify-content: space-between;
  gap: 20px;
  padding: 18px 24px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #ffffff;
}

.header-actions {
  gap: 10px;
}

.brand {
  color: #2563eb;
  font-size: 20px;
  font-weight: 700;
  text-decoration: none;
}

.app-link {
  padding: 9px 13px;
  border-radius: 8px;
  background: #2563eb;
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
}

.hero-card,
.purpose-card,
.how-card,
.portfolio-card {
  margin-top: 24px;
  padding: 36px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #ffffff;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

h1,
h2,
h3 {
  margin: 0;
}

h1 {
  max-width: 680px;
  font-size: clamp(32px, 5vw, 48px);
  line-height: 1.1;
}

h2 {
  font-size: 22px;
}

h3 {
  font-size: 17px;
}

.hero-card > p:last-child,
.purpose-card p,
.portfolio-card p,
.how-card li p {
  color: #64748b;
  line-height: 1.65;
}

.hero-card > p:last-child {
  max-width: 700px;
  margin: 18px 0 0;
  font-size: 18px;
}

.purpose-card p,
.portfolio-card p {
  max-width: 760px;
  margin: 12px 0 0;
}

.how-card {
  scroll-margin-top: 24px;
}

.how-card ol {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin: 22px 0 0;
  padding: 0;
  list-style: none;
}

.how-card li {
  padding: 20px;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  background: #f8fbff;
}

.how-card li > span {
  display: grid;
  width: 32px;
  height: 32px;
  margin-bottom: 16px;
  place-items: center;
  border-radius: 9px;
  background: #2563eb;
  color: #ffffff;
  font-weight: 700;
}

.how-card li p {
  margin: 8px 0 0;
  font-size: 14px;
}

@media (max-width: 700px) {
  .about-page {
    padding: 16px;
  }

  .about-header {
    align-items: flex-start;
    flex-direction: column;
    padding: 18px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .hero-card,
  .purpose-card,
  .how-card,
  .portfolio-card {
    padding: 24px;
  }

  .how-card ol {
    grid-template-columns: 1fr;
  }
}
</style>
