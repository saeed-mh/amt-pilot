<script setup>
import { onMounted, ref } from 'vue'

import AppHeader from '@/components/AppHeader.vue'
import ApplicationList from '@/components/ApplicationList.vue'
import ProcessList from '@/components/ProcessList.vue'
import { getCurrentUser } from '@/services/user'

const user = ref(null)
const isLoading = ref(true)
const loadError = ref('')
const applicationsVersion = ref(0)
const processesVersion = ref(0)

async function loadProfile() {
  try {
    user.value = await getCurrentUser()
  } catch (error) {
    loadError.value = error.message
  } finally {
    isLoading.value = false
  }
}

function refreshApplications() {
  applicationsVersion.value += 1
}

function refreshProcesses() {
  processesVersion.value += 1
}

onMounted(loadProfile)
</script>

<template>
  <main class="dashboard-page">
    <section class="dashboard">
      <AppHeader :email="user?.email" />

      <section class="welcome">
        <p class="eyebrow">Dashboard</p>
        <h1>
          {{ user ? `Welcome, ${user.email}` : 'Welcome to AmtPilot' }}
        </h1>
        <p>Manage your applications and find administrative processes relevant to your city.</p>
      </section>

      <p v-if="isLoading" class="status-message">Loading your profile...</p>

      <p v-else-if="loadError" class="status-message error" role="alert">
        {{ loadError }}
      </p>

      <ApplicationList
        v-if="user"
        :key="applicationsVersion"
        class="application-section"
        @applications-changed="refreshProcesses"
      />

      <ProcessList
        v-if="user"
        :key="processesVersion"
        class="process-section"
        :city="user.city || 'Dortmund'"
        compact
        @application-created="refreshApplications"
      />
    </section>
  </main>
</template>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  padding: 24px;
  background: #f4f7fb;
  color: #1f2937;
}

.dashboard {
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
}

.welcome {
  margin-top: 24px;
  padding: 36px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.application-section,
.process-section {
  margin-top: 24px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0;
  font-size: 30px;
}

.welcome p:last-child {
  max-width: 640px;
  margin: 12px 0 0;
  color: #6b7280;
  line-height: 1.6;
}

.status-message {
  margin-top: 24px;
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
  .dashboard-page {
    padding: 16px;
  }

  .welcome {
    padding: 20px;
  }
}
</style>
