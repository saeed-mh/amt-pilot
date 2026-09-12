<script setup>
import { ref, watch } from 'vue'

import { createApplication } from '@/services/application'
import { getProcesses, getProcessRequirements } from '@/services/process'

const emit = defineEmits(['application-created'])

const props = defineProps({
  city: {
    type: String,
    default: 'Dortmund',
  },
})

const processes = ref([])
const isLoading = ref(false)
const errorMessage = ref('')
const selectedProcessId = ref(null)
const requirements = ref([])
const isLoadingRequirements = ref(false)
const requirementsError = ref('')
const creatingProcessId = ref(null)
const applicationMessage = ref('')
const applicationError = ref('')

async function loadProcesses() {
  isLoading.value = true
  errorMessage.value = ''
  selectedProcessId.value = null
  requirements.value = []
  requirementsError.value = ''
  applicationMessage.value = ''
  applicationError.value = ''

  try {
    processes.value = await getProcesses(props.city || 'Dortmund')
  } catch (error) {
    processes.value = []
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function startApplication(process) {
  creatingProcessId.value = process.id
  applicationMessage.value = ''
  applicationError.value = ''

  try {
    const application = await createApplication(process.id)
    applicationMessage.value = `${process.title} application created successfully.`
    emit('application-created', application)
  } catch (error) {
    applicationError.value = error.message
  } finally {
    creatingProcessId.value = null
  }
}

async function toggleRequirements(process) {
  if (selectedProcessId.value === process.id) {
    selectedProcessId.value = null
    requirements.value = []
    requirementsError.value = ''
    return
  }

  selectedProcessId.value = process.id
  requirements.value = []
  requirementsError.value = ''
  isLoadingRequirements.value = true

  try {
    requirements.value = await getProcessRequirements(process.id)
  } catch (error) {
    requirementsError.value = error.message
  } finally {
    isLoadingRequirements.value = false
  }
}

watch(
  () => props.city,
  () => loadProcesses(),
  { immediate: true },
)
</script>

<template>
  <section class="process-card">
    <h2>Available processes</h2>
    <p class="description">Administrative processes available in {{ city || 'Dortmund' }}.</p>

    <p v-if="applicationMessage" class="feedback success" role="status">
      {{ applicationMessage }}
    </p>

    <p v-if="applicationError" class="feedback error" role="alert">
      {{ applicationError }}
    </p>

    <p v-if="isLoading">Loading processes...</p>
    <p v-else-if="errorMessage" class="error" role="alert">
      {{ errorMessage }}
    </p>

    <p v-else-if="processes.length === 0">No processes were found for this city.</p>

    <ul v-else class="process-list">
      <li v-for="process in processes" :key="process.id">
        <h3>{{ process.title }}</h3>
        <p>{{ process.domain }}</p>
        <small>Provided by {{ process.authorityName }}</small>

        <div class="process-actions">
          <button
            class="requirements-button"
            type="button"
            :aria-expanded="selectedProcessId === process.id"
            @click="toggleRequirements(process)"
          >
            {{ selectedProcessId === process.id ? 'Hide requirements' : 'View requirements' }}
          </button>

          <button
            class="start-button"
            type="button"
            :disabled="creatingProcessId !== null"
            @click="startApplication(process)"
          >
            {{ creatingProcessId === process.id ? 'Creating...' : 'Start application' }}
          </button>
        </div>

        <div v-if="selectedProcessId === process.id" class="requirements">
          <p v-if="isLoadingRequirements">Loading requirements...</p>

          <p v-else-if="requirementsError" class="error" role="alert">
            {{ requirementsError }}
          </p>

          <p v-else-if="requirements.length === 0">No requirements have been added yet.</p>

          <ul v-else class="requirements-list">
            <li v-for="requirement in requirements" :key="requirement.id">
              <div>
                <strong>{{ requirement.title }}</strong>
                <small>{{ requirement.required ? 'Required' : 'Optional' }}</small>
              </div>

              <a
                v-if="requirement.sourceUrl"
                :href="requirement.sourceUrl"
                target="_blank"
                rel="noopener noreferrer"
              >
                Official source
              </a>
            </li>
          </ul>
        </div>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.process-card {
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  background: #ffffff;
}

.process-card h2 {
  margin: 0;
}

.description {
  margin: 0.5rem 0 1.25rem;
  color: #64748b;
}

.process-list {
  display: grid;
  gap: 0.75rem;
  padding: 0;
  list-style: none;
}

.process-list > li {
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
}

.process-list h3 {
  margin: 0 0 0.25rem;
}

.process-list > li > p {
  margin: 0 0 0.5rem;
  color: #2563eb;
}

.process-list > li > small {
  color: #64748b;
}

.process-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 1rem;
}

.requirements-button,
.start-button {
  padding: 0.6rem 1rem;
  border: 1px solid #2563eb;
  border-radius: 0.5rem;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.requirements-button {
  background: #ffffff;
  color: #2563eb;
}

.requirements-button:hover {
  background: #eff6ff;
}

.start-button {
  background: #2563eb;
  color: #ffffff;
}

.start-button:hover:not(:disabled) {
  background: #1d4ed8;
}

.start-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.requirements {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.requirements-list {
  display: grid;
  gap: 0.5rem;
  padding: 0;
  list-style: none;
}

.requirements-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.75rem;
  border-radius: 0.5rem;
  background: #f8fafc;
}

.requirements-list strong,
.requirements-list small {
  display: block;
}

.requirements-list small {
  margin-top: 0.25rem;
  color: #64748b;
}

.requirements-list a {
  color: #2563eb;
  font-weight: 600;
  text-decoration: none;
}

.error {
  color: #b91c1c;
}

.feedback {
  padding: 0.75rem;
  border-radius: 0.5rem;
}

.feedback.success {
  background: #ecfdf5;
  color: #047857;
}

.feedback.error {
  background: #fef2f2;
}
</style>
