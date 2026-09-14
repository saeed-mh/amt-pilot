<script setup>
import { onMounted, ref } from 'vue'

import AppHeader from '@/components/AppHeader.vue'
import ProcessList from '@/components/ProcessList.vue'
import { getCurrentUser } from '@/services/user'

const user = ref(null)
const isLoading = ref(true)
const loadError = ref('')

async function loadProfile() {
  try {
    user.value = await getCurrentUser()
  } catch (error) {
    loadError.value = error.message
  } finally {
    isLoading.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <main class="processes-page">
    <section class="processes-layout">
      <AppHeader :email="user?.email" />

      <section class="page-intro">
        <RouterLink class="back-link" to="/dashboard">Back to dashboard</RouterLink>
        <p class="eyebrow">Process catalog</p>
        <h1>Browse all processes</h1>
        <p>Search for the administrative process that matches what you need to do.</p>
      </section>

      <p v-if="isLoading" class="status-message">Loading processes...</p>

      <p v-else-if="loadError" class="status-message error" role="alert">
        {{ loadError }}
      </p>

      <ProcessList v-else-if="user" class="process-list-section" :city="user.city || 'Dortmund'" />
    </section>
  </main>
</template>

<style scoped>
.processes-page {
  min-height: 100vh;
  padding: 24px;
  background: #f4f7fb;
  color: #1f2937;
}

.processes-layout {
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
}

.page-intro {
  margin-top: 24px;
  padding: 36px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #ffffff;
}

.back-link {
  display: inline-block;
  margin-bottom: 24px;
  color: #2563eb;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
}

.back-link::before {
  content: '←';
  margin-right: 8px;
}

.back-link:hover {
  text-decoration: underline;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  font-size: 30px;
}

.page-intro > p:last-child {
  max-width: 640px;
  margin: 10px 0 0;
  color: #6b7280;
  line-height: 1.5;
}

.process-list-section,
.status-message {
  margin-top: 24px;
}

.status-message {
  padding: 14px;
  border-radius: 8px;
  background: #ffffff;
  color: #6b7280;
}

.status-message.error {
  background: #fef2f2;
  color: #b91c1c;
}

@media (max-width: 600px) {
  .processes-page {
    padding: 16px;
  }

  .page-intro {
    padding: 20px;
  }
}
</style>
