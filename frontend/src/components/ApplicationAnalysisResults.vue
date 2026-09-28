<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'

import { t, translateCode } from '@/i18n'
import {
  analyzeApplication,
  getApplication,
  getApplicationAdvice,
  getApplicationAnalyses,
  saveApplicationAdviceAnswers,
} from '@/services/application'

const POLL_INTERVAL_MS = 2_000
const ADDITIONAL_CONTEXT_KEY = '_additional_context'
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
const isSavingAnswers = ref(false)
const errorMessage = ref('')
const answerDrafts = ref({})
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

const hasAnswerDrafts = computed(() =>
  Object.values(answerDrafts.value).some((answer) => answer.trim().length > 0),
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
      answerDrafts.value = Object.fromEntries([
        ...(loadedAdvice?.questionsForUser ?? []).map((question) => [
          question,
          loadedAdvice.userAnswers?.[question] ?? '',
        ]),
        [ADDITIONAL_CONTEXT_KEY, loadedAdvice?.userAnswers?.[ADDITIONAL_CONTEXT_KEY] ?? ''],
      ])
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

async function saveAnswersAndReanalyze() {
  const answers = Object.fromEntries(
    Object.entries(answerDrafts.value)
      .map(([question, answer]) => [question, answer.trim()])
      .filter(([, answer]) => answer.length > 0),
  )

  if (Object.keys(answers).length === 0) {
    return
  }

  isSavingAnswers.value = true
  errorMessage.value = ''

  try {
    await saveApplicationAdviceAnswers(props.application.id, answers)
    const updatedApplication = await analyzeApplication(props.application.id)

    advice.value = null
    analyses.value = []
    emit('application-updated', updatedApplication)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isSavingAnswers.value = false
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

function guideSectionLabel(section) {
  return translateCode('guideSection', section, formatFieldName(section))
}

function referenceValue(reference, camelCaseName, snakeCaseName) {
  return reference?.[camelCaseName] ?? reference?.[snakeCaseName]
}

function formatVerifiedDate(reference) {
  const value = referenceValue(reference, 'verifiedAt', 'verified_at')

  if (!value) {
    return ''
  }

  return new Intl.DateTimeFormat(undefined, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(new Date(`${value}T00:00:00`))
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
          <span class="advice-eyebrow">{{ t('applications.applicationReview') }}</span>
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
                <small v-if="assessment.officialSourceUrl" class="official-requirement-source">
                  {{ t('applications.officialRequirementSource') }}:
                  <a :href="assessment.officialSourceUrl" target="_blank" rel="noopener noreferrer">
                    {{ assessment.officialSourceTitle || t('catalog.officialSource') }}
                  </a>
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

        <section class="result-section clarification-section">
          <h5>{{ t('applications.questionsForYou') }}</h5>
          <p>{{ t('applications.answerQuestionsDescription') }}</p>
          <form class="clarification-form" @submit.prevent="saveAnswersAndReanalyze">
            <label
              v-for="(question, index) in advice.questionsForUser"
              :key="question"
              :for="`clarification-${application.id}-${index}`"
            >
              <span>{{ question }}</span>
              <textarea
                :id="`clarification-${application.id}-${index}`"
                v-model="answerDrafts[question]"
                rows="3"
                maxlength="2000"
                :placeholder="t('applications.answerPlaceholder')"
                :disabled="isSavingAnswers"
              ></textarea>
            </label>

            <label :for="`additional-context-${application.id}`">
              <span>{{ t('applications.additionalContextLabel') }}</span>
              <textarea
                :id="`additional-context-${application.id}`"
                v-model="answerDrafts[ADDITIONAL_CONTEXT_KEY]"
                rows="3"
                maxlength="2000"
                :placeholder="t('applications.additionalContextPlaceholder')"
                :disabled="isSavingAnswers"
              ></textarea>
            </label>

            <button
              type="submit"
              class="reanalyze-button"
              :disabled="isSavingAnswers || !hasAnswerDrafts"
            >
              {{
                isSavingAnswers
                  ? t('applications.savingAnswers')
                  : t('applications.saveAnswersAndReanalyze')
              }}
            </button>
          </form>
        </section>

        <section
          v-if="advice.officialSourceReferences?.length"
          class="result-section grounded-sources"
        >
          <div class="grounded-heading">
            <h5>{{ t('applications.officialInformationUsed') }}</h5>
            <span class="grounded-badge">{{ t('applications.sourceBacked') }}</span>
          </div>
          <p class="grounded-description">
            {{ t('applications.groundedDescription') }}
          </p>
          <ul class="grounded-reference-list">
            <li v-for="reference in advice.officialSourceReferences" :key="reference.section">
              <strong>{{ guideSectionLabel(reference.section) }}</strong>
              <ul>
                <li v-for="statement in reference.statements" :key="statement">
                  {{ statement }}
                </li>
              </ul>
              <div class="source-meta">
                <a
                  :href="referenceValue(reference, 'sourceUrl', 'source_url')"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  {{ referenceValue(reference, 'sourceTitle', 'source_title') }}
                </a>
                <span v-if="formatVerifiedDate(reference)">
                  {{ t('catalog.verifiedOn', { date: formatVerifiedDate(reference) }) }}
                </span>
              </div>
            </li>
          </ul>
        </section>

        <small class="disclaimer">{{ advice.disclaimer }}</small>
      </article>

      <details v-for="analysis in analyses" :key="analysis.id" class="analysis-result">
        <summary class="document-heading">
          <span class="document-heading-copy">
            <strong>{{ analysis.originalFilename }}</strong>
            <small>
              {{ analysis.documentType }}
              <template v-if="analysis.primaryLanguage">
                | {{ analysis.primaryLanguage.toUpperCase() }}
              </template>
            </small>
          </span>
          <span class="document-toggle-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <path d="m7 10 5 5 5-5" />
            </svg>
          </span>
        </summary>

        <div class="document-details">
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
                  <small v-if="field.evidence" class="field-evidence">
                    <span class="evidence-label">{{ t('applications.evidence') }}:</span>
                    <span>{{ field.evidence }}</span>
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
        </div>
      </details>
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
  border: 1px solid #dbeafe;
  border-radius: 0.65rem;
  background: #ffffff;
  overflow: hidden;
}

.analysis-result[open] {
  border-color: #bfdbfe;
  box-shadow: 0 0.35rem 1rem rgb(37 99 235 / 8%);
}

.document-details {
  padding: 0 1rem 1rem;
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
  align-items: center;
  justify-content: center;
  padding: 0.3rem 0.55rem;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 800;
  text-align: center;
  white-space: nowrap;
}

.assessment-status {
  align-self: start;
}

.readiness-ready_to_submit,
.assessment-satisfied {
  background: #dcfce7;
  color: #166534;
}

.assessment-not_applicable {
  background: #e2e8f0;
  color: #475569;
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

.official-requirement-source {
  display: block;
  margin-top: 0.35rem;
}

.official-requirement-source a,
.source-meta a {
  color: #1d4ed8;
  font-weight: 700;
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

.clarification-section {
  padding: 0.85rem;
  border: 1px solid #c4b5fd;
  border-radius: 0.55rem;
  background: #faf5ff;
}

.clarification-section > p {
  margin-bottom: 0.75rem;
}

.clarification-form {
  display: grid;
  gap: 0.75rem;
}

.clarification-form label {
  display: grid;
  gap: 0.35rem;
  color: #334155;
  font-size: 0.85rem;
  font-weight: 700;
}

.clarification-form textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 0.65rem;
  resize: vertical;
  border: 1px solid #cbd5e1;
  border-radius: 0.45rem;
  background: #ffffff;
  color: #1e293b;
  font: inherit;
  font-weight: 400;
  line-height: 1.45;
}

.clarification-form textarea:focus {
  border-color: #7c3aed;
  outline: 3px solid #ede9fe;
}

.reanalyze-button {
  justify-self: start;
  padding: 0.6rem 0.9rem;
  border: 1px solid #7c3aed;
  border-radius: 0.45rem;
  background: #7c3aed;
  color: #ffffff;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.reanalyze-button:hover:not(:disabled) {
  background: #6d28d9;
}

.reanalyze-button:disabled,
.clarification-form textarea:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.grounded-sources {
  padding: 0.85rem;
  border: 1px solid #bbf7d0;
  border-radius: 0.55rem;
  background: #f0fdf4;
}

.grounded-heading,
.source-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.grounded-heading h5 {
  margin: 0;
  color: #166534;
}

.grounded-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 999px;
  background: #dcfce7;
  color: #166534;
  font-size: 0.7rem;
  font-weight: 800;
}

.grounded-description {
  margin-top: 0.45rem;
}

.grounded-reference-list {
  display: grid;
  gap: 0.65rem;
  padding: 0;
  list-style: none;
}

.grounded-reference-list > li {
  padding: 0.75rem;
  border-radius: 0.5rem;
  background: #ffffff;
}

.grounded-reference-list ul {
  margin: 0.4rem 0 0;
  padding-left: 1.2rem;
}

.source-meta {
  align-items: flex-start;
  margin-top: 0.55rem;
  color: #64748b;
  font-size: 0.75rem;
}

.disclaimer {
  display: block;
  margin-top: 1rem;
  color: #64748b;
  line-height: 1.4;
}

.document-heading {
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  cursor: pointer;
  list-style: none;
  transition: background-color 0.2s ease;
}

.document-heading:hover {
  background: #f8fafc;
}

.document-heading:focus-visible {
  outline: 3px solid #93c5fd;
  outline-offset: -3px;
}

.document-heading::-webkit-details-marker {
  display: none;
}

.document-heading-copy {
  display: grid;
  min-width: 0;
  gap: 0.2rem;
}

.document-heading strong {
  overflow-wrap: anywhere;
}

.document-heading small {
  color: #64748b;
}

.document-toggle-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  background: #eff6ff;
  color: #2563eb;
  transition:
    transform 0.2s ease,
    background-color 0.2s ease;
}

.document-toggle-icon svg {
  width: 1.1rem;
  height: 1.1rem;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.analysis-result[open] .document-heading {
  border-bottom: 1px solid #dbeafe;
}

.analysis-result[open] .document-toggle-icon {
  transform: rotate(180deg);
  background: #dbeafe;
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

.field-evidence {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
  margin-top: 0.15rem;
}

.evidence-label {
  font-weight: 700;
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
  .field,
  .requirement-assessments li {
    grid-template-columns: 1fr;
  }

  .advice-heading {
    display: grid;
  }

  .grounded-heading,
  .source-meta {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
