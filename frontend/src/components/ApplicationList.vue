<script setup>
import { onMounted, ref } from 'vue'

import { getApplications } from '@/services/application'

const applications = ref([])
const isLoading = ref(true)
const errorMessage = ref('')

async function loadApplications() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    applications.value = await getApplications()
  } catch (error) {
    applications.value = []
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

function formatDate(value) {
  if (!value) {
    return 'Recently created'
  }

  return new Intl.DateTimeFormat('en-GB', {
    dateStyle: 'medium',
  }).format(new Date(value))
}

onMounted(loadApplications)
</script>

<template>
  <section class="application-card">
    <h2>My applications</h2>
    <p class="description">Track the applications you have started.</p>

    <p v-if="isLoading">Loading applications...</p>

    <p v-else-if="errorMessage" class="error" role="alert">
      {{ errorMessage }}
    </p>

    <p v-else-if="applications.length === 0">You have not started an application yet.</p>

    <ul v-else class="application-list">
      <li v-for="application in applications" :key="application.id">
        <div class="application-heading">
          <div>
            <h3>{{ application.processTitle }}</h3>
            <small>Created {{ formatDate(application.createdAt) }}</small>
          </div>

          <span class="status">{{ application.status }}</span>
        </div>

        <div class="progress-heading">
          <span>Completeness</span>
          <strong>{{ application.completeness }}%</strong>
        </div>

        <div
          class="progress-track"
          role="progressbar"
          aria-label="Application completeness"
          aria-valuemin="0"
          aria-valuemax="100"
          :aria-valuenow="application.completeness"
        >
          <div class="progress-value" :style="{ width: `${application.completeness}%` }"></div>
        </div>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.application-card {
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  background: #ffffff;
}

.application-card h2 {
  margin: 0;
}

.description {
  margin: 0.5rem 0 1.25rem;
  color: #64748b;
}

.application-list {
  display: grid;
  gap: 0.75rem;
  padding: 0;
  list-style: none;
}

.application-list > li {
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
}

.application-heading,
.progress-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.application-heading h3 {
  margin: 0 0 0.25rem;
}

.application-heading small {
  color: #64748b;
}

.status {
  padding: 0.3rem 0.6rem;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 0.75rem;
  font-weight: 700;
}

.progress-heading {
  margin-top: 1rem;
  font-size: 0.875rem;
}

.progress-track {
  height: 0.5rem;
  margin-top: 0.5rem;
  overflow: hidden;
  border-radius: 999px;
  background: #e2e8f0;
}

.progress-value {
  height: 100%;
  border-radius: inherit;
  background: #2563eb;
}

.error {
  color: #b91c1c;
}

@media (max-width: 600px) {
  .application-heading {
    align-items: flex-start;
  }
}
</style>
