<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'

import { t, translateCode } from '@/i18n'
import {
  getApplication,
  getApplicationAdvice,
  getApplicationAnalyses,
} from '@/services/application'

const POLL_INTERVAL_MS = 2_000
const RESULT_STATUSES = new Set([
  'ACTION_REQUIRED',
  'READY_TO_SUBMIT',
  'NEEDS_REVIEW',
  'SUBMITTED',
  'COMPLETED',
])

const props = defineProps({
  application: {
    type: Object,
    required: true,
  },
  expanded: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['application-updated'])

const analyses = ref([])
const advice = ref(null)
const isLoading = ref(false)
const errorMessage = ref('')
let pollTimer = null
let loadVersion = 0

const isVisible = computed(
  () =>
    props.expanded &&
    (props.application.status === 'ANALYZING' ||
      RESULT_STATUSES.has(props.application.status) ||
      advice.value ||
      analyses.value.length > 0),
)

function stopPolling() {
  if (pollTimer !== null) {
    window.clearTimeout(pollTimer)
    pollTimer = null
  }
}

function schedulePoll(applicationId) {
  stopPolling()

  pollTimer = window.setTimeout(() => {
    pollApplication(applicationId)
  }, POLL_INTERVAL_MS)
}

async function pollApplication(applicationId) {
  try {
    const updatedApplication = await getApplication(applicationId)

    if (props.application.id !== applicationId) {
      return
    }

    emit('application-updated', updatedApplication)

    if (updatedApplication.status === 'ANALYZING') {
      schedulePoll(applicationId)
    } else {
      stopPolling()
    }
  } catch (error) {
    stopPolling()
    errorMessage.value = error.message
  }
}

async function loadResults(applicationId) {
  const currentVersion = ++loadVersion

  isLoading.value = true
  errorMessage.value = ''

  try {
    const [loadedAnalyses, loadedAdvice] = await Promise.all([
      getApplicationAnalyses(applicationId),
      getApplicationAdvice(applicationId),
    ])

    if (currentVersion === loadVersion && props.application.id === applicationId) {
      analyses.value = loadedAnalyses
      advice.value = loadedAdvice
    }
  } catch (error) {
    if (currentVersion === loadVersion) {
      errorMessage.value = error.message
    }
  } finally {
    if (currentVersion === loadVersion) {
      isLoading.value = false
    }
  }
}

function synchronize(applicationId, status) {
  stopPolling()
  errorMessage.value = ''

  if (status === 'ANALYZING') {
    analyses.value = []
    advice.value = null
    schedulePoll(applicationId)
    return
  }

  if (RESULT_STATUSES.has(status)) {
    loadResults(applicationId)
    return
  }

  loadVersion += 1
  analyses.value = []
  advice.value = null
  isLoading.value = false
}

function formatFieldName(name) {
  if (!name) {
    return ''
  }

  return name.replace(/_/g, ' ').replace(/\b\w/g, (letter) => letter.toUpperCase())
}

function adviceStatusLabel(status) {
  return translateCode('adviceStatus', status, status)
}

function readinessLabel(readiness) {
  return translateCode('status', readiness, readiness)
}

watch(
  [() => props.application.id, () => props.application.status],
  ([applicationId, status]) => synchronize(applicationId, status),
  { immediate: true },
)

onBeforeUnmount(() => {
  loadVersion += 1
  stopPolling()
})
</script>

<template>
  <section v-if="isVisible" class="analysis-panel">
    <header class="analysis-title">
      <span class="ai-badge">AI</span>
      <h4>{{ t('applications.analysisResults') }}</h4>
    </header>

    <div v-if="application.status === 'ANALYZING'" class="analysis-progress" role="status">
      <span class="spinner" aria-hidden="true"></span>
      <span>{{ t('applications.analysisProgress') }}</span>
    </div>

    <p v-else-if="isLoading" class="analysis-message">
      {{ t('applications.loadingAnalysis') }}
    </p>

    <p v-else-if="errorMessage" class="analysis-message error" role="alert">
      {{ errorMessage }}
    </p>

    <p v-else-if="!advice && analyses.length === 0" class="analysis-message">
      {{ t('applications.noAnalysisResults') }}
    </p>

    <div v-else class="analysis-results">
      <article v-if="advice" class="application-advice">
        <header class="advice-heading">
          <div>
            <span class="advice-eyebrow">{{ t('applications.applicationGuidance') }}</span>
            <h5>{{ readinessLabel(advice.readiness) }}</h5>
          </div>
          <span class="readiness-badge" :class="`readiness-${advice.readiness.toLowerCase()}`">
            {{ readinessLabel(advice.readiness) }}
          </span>
        </header>

        <p class="advice-summary">{{ advice.summary }}</p>

        <section v-if="advice.requirementAssessments?.length" class="result-section">
          <h5>{{ t('applications.requirementAssessment') }}</h5>
          <ul class="requirement-assessments">
            <li
              v-for="assessment in advice.requirementAssessments"
              :key="assessment.requirementCode"
            >
              <span
                class="assessment-status"
                :class="`assessment-${assessment.status.toLowerCase()}`"
              >
                {{ adviceStatusLabel(assessment.status) }}
              </span>
              <div>
                <strong>{{ formatFieldName(assessment.requirementCode) }}</strong>
                <p>{{ assessment.explanation }}</p>
                <small v-if="assessment.supportingDocuments?.length">
                  {{ t('applications.supportingDocuments') }}:
                  {{ assessment.supportingDocuments.join(', ') }}
                </small>
              </div>
            </li>
          </ul>
        </section>

        <section v-if="advice.inconsistencies?.length" class="result-section issue-section">
          <h5>{{ t('applications.inconsistencies') }}</h5>
          <ul>
            <li v-for="item in advice.inconsistencies" :key="item">{{ item }}</li>
          </ul>
        </section>

        <section v-if="advice.nextSteps?.length" class="result-section next-steps">
          <h5>{{ t('applications.nextSteps') }}</h5>
          <ol>
            <li v-for="step in advice.nextSteps" :key="step">{{ step }}</li>
          </ol>
        </section>

        <section v-if="advice.questionsForUser?.length" class="result-section">
          <h5>{{ t('applications.questionsForYou') }}</h5>
          <ul>
            <li v-for="question in advice.questionsForUser" :key="question">
              {{ question }}
            </li>
          </ul>
        </section>

        <small class="disclaimer">{{ advice.disclaimer }}</small>
      </article>

      <article v-for="analysis in analyses" :key="analysis.id" class="analysis-result">
        <header class="document-heading">
          <strong>{{ analysis.originalFilename }}</strong>
          <small>
            {{ analysis.documentType }}
            <template v-if="analysis.primaryLanguage">
              | {{ analysis.primaryLanguage.toUpperCase() }}
            </template>
          </small>
        </header>

        <section class="result-section">
          <h5>{{ t('applications.analysisSummary') }}</h5>
          <p>{{ analysis.summary }}</p>
        </section>

        <section class="result-section">
          <h5>{{ t('applications.extractedInformation') }}</h5>

          <p v-if="!analysis.extractedFields?.length" class="empty-result">
            {{ t('applications.noExtractedInformation') }}
          </p>

          <dl v-else class="field-list">
            <div v-for="field in analysis.extractedFields" :key="field.name" class="field">
              <dt>{{ formatFieldName(field.name) }}</dt>
              <dd>
                <strong>{{ field.value }}</strong>
                <small v-if="field.evidence">
                  {{ t('applications.evidence') }}: {{ field.evidence }}
                  <template v-if="field.pageNumber">
                    ({{ t('applications.page') }} {{ field.pageNumber }})
                  </template>
                </small>
              </dd>
            </div>
          </dl>
        </section>

        <section v-if="analysis.missingOrUnclear?.length" class="result-section issue-section">
          <h5>{{ t('applications.missingInformation') }}</h5>
          <ul>
            <li v-for="item in analysis.missingOrUnclear" :key="item">
              {{ item }}
            </li>
          </ul>
        </section>

        <section v-if="analysis.warnings?.length" class="result-section warning-section">
          <h5>{{ t('applications.warnings') }}</h5>
          <ul>
            <li v-for="warning in analysis.warnings" :key="warning">
              {{ warning }}
            </li>
          </ul>
        </section>
      </article>
    </div>
  </section>
</template>

<style scoped>
.analysis-panel {
  margin-top: 1rem;
  padding: 1rem;
  border: 1px solid #bfdbfe;
  border-radius: 0.75rem;
  background: #f8fbff;
}

.analysis-title,
.document-heading {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.analysis-title h4 {
  margin: 0;
  color: #1e3a8a;
}

.ai-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 0.55rem;
  background: #2563eb;
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 800;
}

.analysis-progress {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  margin-top: 1rem;
  color: #1d4ed8;
}

.spinner {
  width: 1rem;
  height: 1rem;
  border: 2px solid #bfdbfe;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.analysis-message {
  margin: 1rem 0 0;
  color: #64748b;
}

.analysis-message.error {
  color: #b91c1c;
}

.analysis-results {
  display: grid;
  gap: 1rem;
  margin-top: 1rem;
}

.analysis-result {
  padding: 1rem;
  border: 1px solid #dbeafe;
  border-radius: 0.65rem;
  background: #ffffff;
}

.application-advice {
  padding: 1rem;
  border: 1px solid #93c5fd;
  border-radius: 0.65rem;
  background: #eff6ff;
}

.advice-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.advice-heading h5 {
  margin: 0.2rem 0 0;
  color: #172554;
  font-size: 1.1rem;
}

.advice-eyebrow {
  color: #2563eb;
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.readiness-badge,
.assessment-status {
  display: inline-flex;
  padding: 0.3rem 0.55rem;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 800;
  white-space: nowrap;
}

.readiness-ready_to_submit,
.assessment-satisfied {
  background: #dcfce7;
  color: #166534;
}

.readiness-action_required,
.assessment-missing {
  background: #fef3c7;
  color: #92400e;
}

.readiness-needs_review,
.assessment-needs_review {
  background: #fee2e2;
  color: #991b1b;
}

.advice-summary {
  margin: 1rem 0 0;
  color: #334155;
  line-height: 1.6;
}

.requirement-assessments {
  display: grid;
  gap: 0.65rem;
  padding: 0;
  list-style: none;
}

.requirement-assessments li {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 0.5rem;
  background: #ffffff;
}

.requirement-assessments p {
  margin-top: 0.25rem;
}

.requirement-assessments small {
  color: #64748b;
}

.next-steps {
  padding: 0.75rem;
  border-radius: 0.5rem;
  background: #ffffff;
}

.next-steps ol {
  margin: 0;
  padding-left: 1.25rem;
  color: #334155;
  line-height: 1.6;
}

.disclaimer {
  display: block;
  margin-top: 1rem;
  color: #64748b;
  line-height: 1.4;
}

.document-heading {
  align-items: flex-start;
  justify-content: space-between;
}

.document-heading strong {
  overflow-wrap: anywhere;
}

.document-heading small {
  color: #64748b;
  text-align: right;
}

.result-section {
  margin-top: 1rem;
}

.result-section h5 {
  margin: 0 0 0.45rem;
  color: #334155;
  font-size: 0.9rem;
}

.result-section p,
.result-section ul {
  margin: 0;
  color: #475569;
  line-height: 1.55;
}

.field-list {
  display: grid;
  gap: 0.6rem;
  margin: 0;
}

.field {
  display: grid;
  grid-template-columns: minmax(120px, 0.35fr) 1fr;
  gap: 0.75rem;
  padding: 0.65rem;
  border-radius: 0.5rem;
  background: #f8fafc;
}

.field dt {
  color: #64748b;
  font-size: 0.85rem;
}

.field dd {
  display: grid;
  gap: 0.25rem;
  margin: 0;
  color: #1e293b;
}

.field dd small {
  color: #64748b;
  line-height: 1.4;
}

.issue-section,
.warning-section {
  padding: 0.75rem;
  border-radius: 0.5rem;
}

.issue-section {
  background: #fef2f2;
}

.issue-section h5,
.issue-section ul {
  color: #991b1b;
}

.warning-section {
  background: #fffbeb;
}

.warning-section h5,
.warning-section ul {
  color: #92400e;
}

.empty-result {
  color: #64748b;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 600px) {
  .document-heading,
  .field,
  .requirement-assessments li {
    grid-template-columns: 1fr;
  }

  .document-heading,
  .advice-heading {
    display: grid;
  }

  .document-heading small {
    text-align: left;
  }
}
</style>
