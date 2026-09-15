<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'

import ActionConfirmation from '@/components/ActionConfirmation.vue'
import { createApplication, getApplications } from '@/services/application'
import { getProcesses, getProcessRequirements } from '@/services/process'

const emit = defineEmits(['application-created'])

const props = defineProps({
  city: {
    type: String,
    default: 'Dortmund',
  },
  compact: {
    type: Boolean,
    default: false,
  },
})

const processes = ref([])
const activeProcessIds = ref(new Set())
const searchQuery = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const selectedProcessId = ref(null)
const requirements = ref([])
const isLoadingRequirements = ref(false)
const requirementsError = ref('')
const creatingProcessId = ref(null)
const pendingProcess = ref(null)
const applicationMessage = ref('')
const applicationError = ref('')
let applicationToastTimer = null

const filteredProcesses = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()

  if (!query) {
    return processes.value
  }

  return processes.value.filter((process) =>
    [process.title, process.code, process.domain, process.authorityName].some((value) =>
      value?.toLowerCase().includes(query),
    ),
  )
})

const visibleProcesses = computed(() => {
  if (props.compact && !searchQuery.value.trim()) {
    return filteredProcesses.value.slice(0, 3)
  }

  return filteredProcesses.value
})

function dismissApplicationToast() {
  if (applicationToastTimer) {
    window.clearTimeout(applicationToastTimer)
    applicationToastTimer = null
  }

  applicationMessage.value = ''
}

function showApplicationToast(processTitle) {
  dismissApplicationToast()
  applicationMessage.value = `“${processTitle}” was added to My applications.`

  applicationToastTimer = window.setTimeout(() => {
    applicationMessage.value = ''
    applicationToastTimer = null
  }, 5000)
}

async function loadProcesses() {
  isLoading.value = true
  errorMessage.value = ''
  selectedProcessId.value = null
  requirements.value = []
  requirementsError.value = ''
  dismissApplicationToast()
  applicationError.value = ''
  searchQuery.value = ''

  try {
    const [loadedProcesses, loadedApplications] = await Promise.all([
      getProcesses(props.city || 'Dortmund'),
      getApplications(),
    ])

    processes.value = loadedProcesses
    activeProcessIds.value = new Set(
      loadedApplications
        .filter((application) => application.status !== 'COMPLETED')
        .map((application) => application.processId),
    )
  } catch (error) {
    processes.value = []
    activeProcessIds.value = new Set()
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

function requestApplicationStart(process) {
  if (hasActiveApplication(process.id)) {
    return
  }

  pendingProcess.value = process
}

function hasActiveApplication(processId) {
  return activeProcessIds.value.has(processId)
}

function startButtonLabel(process) {
  if (hasActiveApplication(process.id)) {
    return 'Application already started'
  }

  return creatingProcessId.value === process.id ? 'Creating...' : 'Start application'
}

function cancelApplicationStart() {
  if (creatingProcessId.value) {
    return
  }

  pendingProcess.value = null
}

async function confirmApplicationStart() {
  const process = pendingProcess.value

  if (!process) {
    return
  }

  creatingProcessId.value = process.id
  applicationMessage.value = ''
  applicationError.value = ''

  try {
    const application = await createApplication(process.id)
    activeProcessIds.value = new Set([...activeProcessIds.value, application.processId])
    showApplicationToast(process.title)
    emit('application-created', application)
  } catch (error) {
    applicationError.value = error.message
  } finally {
    creatingProcessId.value = null
    pendingProcess.value = null
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

onBeforeUnmount(dismissApplicationToast)
</script>

<template>
  <section class="process-card">
    <h2>Available processes</h2>
    <p class="description">Administrative processes available in {{ city || 'Dortmund' }}.</p>

    <div class="catalog-tools">
      <label class="search-field" for="process-search">
        <span>Search processes</span>
        <input
          id="process-search"
          v-model="searchQuery"
          type="search"
          placeholder="Try residence permit or passport"
          :disabled="isLoading"
        />
      </label>

      <RouterLink v-if="compact" class="browse-link" to="/processes">
        Browse all processes
      </RouterLink>
    </div>

    <p v-if="applicationError" class="feedback error" role="alert">
      {{ applicationError }}
    </p>

    <p v-if="isLoading">Loading processes...</p>
    <p v-else-if="errorMessage" class="error" role="alert">
      {{ errorMessage }}
    </p>

    <p v-else-if="processes.length === 0">No processes were found for this city.</p>

    <p v-else-if="filteredProcesses.length === 0" class="empty-search">
      No processes match “{{ searchQuery }}”.
    </p>

    <ul v-else class="process-list">
      <li v-for="process in visibleProcesses" :key="process.id">
        <div class="process-summary">
          <h3>{{ process.title }}</h3>
          <p>{{ process.domain }}</p>
          <small>Provided by {{ process.authorityName }}</small>
        </div>

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
            :disabled="creatingProcessId !== null || hasActiveApplication(process.id)"
            @click="requestApplicationStart(process)"
          >
            {{ startButtonLabel(process) }}
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

    <ActionConfirmation
      :open="pendingProcess !== null"
      title="Start a new application?"
      description="AmtPilot will create an application and prepare its checklist for you."
      item-label="Selected process"
      :item-name="pendingProcess?.title"
      confirm-label="Start application"
      pending-label="Starting..."
      :pending="creatingProcessId !== null"
      @cancel="cancelApplicationStart"
      @confirm="confirmApplicationStart"
    />

    <Teleport to="body">
      <Transition name="toast">
        <div v-if="applicationMessage" class="application-toast" role="status" aria-live="polite">
          <span class="toast-icon" aria-hidden="true">✓</span>

          <div class="toast-content">
            <strong>Application created</strong>
            <span>{{ applicationMessage }}</span>
          </div>

          <button
            class="toast-close"
            type="button"
            aria-label="Close notification"
            @click="dismissApplicationToast"
          >
            ×
          </button>
        </div>
      </Transition>
    </Teleport>
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

.catalog-tools {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.search-field {
  display: grid;
  flex: 1;
  gap: 0.5rem;
  max-width: 34rem;
  color: #374151;
  font-size: 0.875rem;
  font-weight: 600;
}

.search-field input {
  width: 100%;
  padding: 0.7rem 0.85rem;
  border: 1px solid #cbd5e1;
  border-radius: 0.5rem;
  background: #ffffff;
  font: inherit;
}

.search-field input:focus {
  border-color: #2563eb;
  outline: 3px solid rgb(37 99 235 / 12%);
}

.search-field input:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.browse-link {
  flex: 0 0 auto;
  padding: 0.65rem 1rem;
  border: 1px solid #2563eb;
  border-radius: 0.5rem;
  color: #2563eb;
  font-weight: 600;
  text-decoration: none;
}

.browse-link:hover {
  background: #eff6ff;
}

.empty-search {
  margin: 0;
  padding: 1rem;
  border-radius: 0.5rem;
  background: #f8fafc;
  color: #64748b;
}

.process-list {
  display: grid;
  gap: 0.75rem;
  padding: 0;
  list-style: none;
}

.process-list > li {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 0.75rem 1.25rem;
  padding: 0.875rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
}

.process-list h3 {
  margin: 0 0 0.25rem;
}

.process-summary > p {
  margin: 0 0 0.5rem;
  color: #2563eb;
}

.process-summary > small {
  color: #64748b;
}

.process-actions {
  display: flex;
  gap: 0.5rem;
  justify-self: end;
}

.requirements-button,
.start-button {
  padding: 0.55rem 0.8rem;
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
  grid-column: 1 / -1;
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

.feedback.error {
  background: #fef2f2;
}

.application-toast {
  position: fixed;
  z-index: 1100;
  top: 1.5rem;
  right: 1.5rem;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: start;
  gap: 0.75rem;
  width: min(calc(100% - 3rem), 25rem);
  padding: 1rem;
  border: 1px solid #a7f3d0;
  border-radius: 0.75rem;
  background: #ffffff;
  box-shadow: 0 16px 40px rgb(15 23 42 / 16%);
}

.toast-icon {
  display: grid;
  width: 2rem;
  height: 2rem;
  place-items: center;
  border-radius: 50%;
  background: #d1fae5;
  color: #047857;
  font-weight: 700;
}

.toast-content {
  display: grid;
  gap: 0.2rem;
  color: #1f2937;
}

.toast-content span {
  color: #64748b;
  font-size: 0.875rem;
  line-height: 1.4;
}

.toast-close {
  padding: 0;
  border: 0;
  background: transparent;
  color: #64748b;
  font: inherit;
  font-size: 1.25rem;
  line-height: 1;
  cursor: pointer;
}

.toast-close:hover {
  color: #1f2937;
}

.toast-enter-active,
.toast-leave-active {
  transition:
    opacity 180ms ease,
    transform 180ms ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(-0.75rem);
}

@media (max-width: 600px) {
  .catalog-tools {
    align-items: stretch;
    flex-direction: column;
  }

  .search-field {
    max-width: none;
  }

  .browse-link {
    text-align: center;
  }

  .process-list > li {
    grid-template-columns: 1fr;
  }

  .process-actions {
    width: 100%;
    justify-self: stretch;
  }

  .requirements-button,
  .start-button {
    flex: 1;
  }

  .requirements {
    grid-column: auto;
  }

  .application-toast {
    top: 1rem;
    right: 1rem;
    width: calc(100% - 2rem);
  }
}
</style>
