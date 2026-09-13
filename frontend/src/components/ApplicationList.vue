<script setup>
import { onMounted, ref } from 'vue'

import {
  deleteApplicationDocument,
  getApplicationDocuments,
  getApplicationChecklist,
  getApplications,
  updateChecklistItem,
  uploadApplicationDocument,
} from '@/services/application'

const applications = ref([])
const isLoading = ref(true)
const errorMessage = ref('')
const selectedApplicationId = ref(null)
const checklistItems = ref([])
const isLoadingChecklist = ref(false)
const checklistError = ref('')
const updatingChecklistItemId = ref(null)
const documents = ref([])
const documentError = ref('')
const documentMessage = ref('')
const uploadingChecklistItemId = ref(null)
const deletingDocumentId = ref(null)

async function loadApplications(showLoading = true) {
  if (showLoading) {
    isLoading.value = true
  }

  errorMessage.value = ''

  try {
    applications.value = await getApplications()
  } catch (error) {
    if (showLoading) {
      applications.value = []
    }

    errorMessage.value = error.message
  } finally {
    if (showLoading) {
      isLoading.value = false
    }
  }
}

async function toggleChecklist(applicationId) {
  if (selectedApplicationId.value === applicationId) {
    selectedApplicationId.value = null
    checklistItems.value = []
    checklistError.value = ''
    documents.value = []
    documentError.value = ''
    documentMessage.value = ''
    return
  }

  selectedApplicationId.value = applicationId
  checklistItems.value = []
  checklistError.value = ''
  documents.value = []
  documentError.value = ''
  documentMessage.value = ''
  isLoadingChecklist.value = true

  try {
    const [loadedChecklistItems, loadedDocuments] = await Promise.all([
      getApplicationChecklist(applicationId),
      getApplicationDocuments(applicationId),
    ])

    checklistItems.value = loadedChecklistItems
    documents.value = loadedDocuments
  } catch (error) {
    checklistError.value = error.message
  } finally {
    isLoadingChecklist.value = false
  }
}

async function changeChecklistItem(item, completed) {
  const previousValue = item.completed

  item.completed = completed
  updatingChecklistItemId.value = item.id
  checklistError.value = ''

  try {
    const updatedItem = await updateChecklistItem(item.id, completed)
    Object.assign(item, updatedItem)
  } catch (error) {
    item.completed = previousValue
    checklistError.value = error.message
    return
  } finally {
    updatingChecklistItemId.value = null
  }

  await loadApplications(false)
}

async function uploadDocument(applicationId, checklistItem, event) {
  const file = event.target.files?.[0]

  if (!file) {
    return
  }

  uploadingChecklistItemId.value = checklistItem.id
  documentError.value = ''
  documentMessage.value = ''

  try {
    const uploadedDocument = await uploadApplicationDocument(applicationId, file, checklistItem.id)

    documents.value.unshift(uploadedDocument)
    checklistItem.completed = true
    documentMessage.value = `${uploadedDocument.originalFilename} uploaded successfully.`
    await loadApplications(false)
  } catch (error) {
    documentError.value = error.message
  } finally {
    uploadingChecklistItemId.value = null
    event.target.value = ''
  }
}

async function removeDocument(applicationId, document) {
  const confirmed = window.confirm(`Delete ${document.originalFilename}?`)

  if (!confirmed) {
    return
  }

  deletingDocumentId.value = document.id
  documentError.value = ''
  documentMessage.value = ''

  try {
    await deleteApplicationDocument(applicationId, document.id)
    documents.value = documents.value.filter((item) => item.id !== document.id)
    checklistItems.value = await getApplicationChecklist(applicationId)
    documentMessage.value = `${document.originalFilename} deleted successfully.`
    await loadApplications(false)
  } catch (error) {
    documentError.value = error.message
  } finally {
    deletingDocumentId.value = null
  }
}

function documentsForChecklistItem(checklistItemId) {
  return documents.value.filter((document) => document.checklistItemId === checklistItemId)
}

function formatFileSize(sizeBytes) {
  if (sizeBytes < 1024) {
    return `${sizeBytes} B`
  }

  if (sizeBytes < 1024 * 1024) {
    return `${(sizeBytes / 1024).toFixed(1)} KB`
  }

  return `${(sizeBytes / (1024 * 1024)).toFixed(1)} MB`
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

        <button
          class="checklist-button"
          type="button"
          :aria-expanded="selectedApplicationId === application.id"
          :disabled="isLoadingChecklist"
          @click="toggleChecklist(application.id)"
        >
          {{ selectedApplicationId === application.id ? 'Close checklist' : 'Open checklist' }}
        </button>

        <div v-if="selectedApplicationId === application.id" class="checklist">
          <p v-if="isLoadingChecklist">Loading checklist...</p>

          <p v-else-if="checklistError" class="error" role="alert">
            {{ checklistError }}
          </p>

          <p v-else-if="checklistItems.length === 0">
            This application does not have checklist items yet.
          </p>

          <ul v-else class="checklist-list">
            <li v-for="item in checklistItems" :key="item.id">
              <label class="checklist-item-label">
                <input
                  type="checkbox"
                  :checked="item.completed"
                  :disabled="updatingChecklistItemId !== null || uploadingChecklistItemId !== null"
                  @change="changeChecklistItem(item, $event.target.checked)"
                />

                <span>
                  <strong>{{ item.title }}</strong>
                  <small>{{ item.required ? 'Required' : 'Optional' }}</small>
                </span>
              </label>

              <div class="document-area">
                <label class="upload-label">
                  <span>
                    {{ uploadingChecklistItemId === item.id ? 'Uploading...' : 'Upload PDF' }}
                  </span>
                  <input
                    type="file"
                    accept=".pdf,application/pdf"
                    :disabled="uploadingChecklistItemId !== null || deletingDocumentId !== null"
                    @change="uploadDocument(application.id, item, $event)"
                  />
                </label>

                <ul v-if="documentsForChecklistItem(item.id).length > 0" class="document-list">
                  <li v-for="document in documentsForChecklistItem(item.id)" :key="document.id">
                    <div class="document-details">
                      <span>{{ document.originalFilename }}</span>
                      <small>{{ formatFileSize(document.sizeBytes) }}</small>
                    </div>

                    <button
                      class="delete-document-button"
                      type="button"
                      :disabled="deletingDocumentId !== null"
                      @click="removeDocument(application.id, document)"
                    >
                      {{ deletingDocumentId === document.id ? 'Deleting...' : 'Delete' }}
                    </button>
                  </li>
                </ul>

                <small v-else class="no-documents">No PDF uploaded yet.</small>
              </div>
            </li>
          </ul>

          <p v-if="documentError" class="feedback error" role="alert">
            {{ documentError }}
          </p>

          <p v-if="documentMessage" class="feedback success" role="status">
            {{ documentMessage }}
          </p>
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

.checklist-button {
  margin-top: 1rem;
  padding: 0.6rem 1rem;
  border: 1px solid #2563eb;
  border-radius: 0.5rem;
  background: #ffffff;
  color: #2563eb;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.checklist-button:hover:not(:disabled) {
  background: #eff6ff;
}

.checklist-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.checklist {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.checklist-list {
  display: grid;
  gap: 0.5rem;
  padding: 0;
  list-style: none;
}

.checklist-list li {
  padding: 0.75rem;
  border-radius: 0.5rem;
  background: #f8fafc;
}

.checklist-item-label {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  cursor: pointer;
}

.checklist-list input {
  width: 1rem;
  height: 1rem;
  margin-top: 0.15rem;
}

.checklist-list strong,
.checklist-list small {
  display: block;
}

.checklist-list small {
  margin-top: 0.25rem;
  color: #64748b;
}

.document-area {
  margin: 0.75rem 0 0 1.75rem;
}

.upload-label {
  display: inline-block;
  padding: 0.45rem 0.75rem;
  border: 1px solid #2563eb;
  border-radius: 0.4rem;
  background: #ffffff;
  color: #2563eb;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
}

.upload-label:hover {
  background: #eff6ff;
}

.upload-label input {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
}

.document-list {
  display: grid;
  gap: 0.35rem;
  margin: 0.75rem 0 0;
  padding: 0;
  list-style: none;
}

.document-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.5rem 0.65rem;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  font-size: 0.875rem;
}

.document-details {
  min-width: 0;
}

.document-details span {
  overflow-wrap: anywhere;
}

.document-details small {
  display: block;
  margin: 0;
}

.delete-document-button {
  flex-shrink: 0;
  padding: 0.35rem 0.6rem;
  border: 1px solid #dc2626;
  border-radius: 0.35rem;
  background: #ffffff;
  color: #dc2626;
  font: inherit;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
}

.delete-document-button:hover:not(:disabled) {
  background: #fef2f2;
}

.delete-document-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.no-documents {
  display: block;
  margin-top: 0.5rem;
}

.feedback {
  margin: 0.75rem 0 0;
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

.error {
  color: #b91c1c;
}

@media (max-width: 600px) {
  .application-heading {
    align-items: flex-start;
  }
}
</style>
