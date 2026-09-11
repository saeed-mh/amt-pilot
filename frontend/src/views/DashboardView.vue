<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getCurrentUser, updateCurrentUser } from '@/services/user'

const router = useRouter()

const user = ref(null)
const isLoading = ref(true)
const isSaving = ref(false)
const loadError = ref('')
const saveError = ref('')
const successMessage = ref('')
const fieldErrors = ref({})

const profileForm = reactive({
  preferredLanguage: '',
  city: '',
  countryOfOrigin: '',
  userType: '',
  timezone: '',
})

function fillProfileForm(profile) {
  profileForm.preferredLanguage = profile.preferredLanguage || ''
  profileForm.city = profile.city || ''
  profileForm.countryOfOrigin = profile.countryOfOrigin || ''
  profileForm.userType = profile.userType || ''
  profileForm.timezone = profile.timezone || ''
}

async function loadProfile() {
  try {
    user.value = await getCurrentUser()
    fillProfileForm(user.value)
  } catch (error) {
    loadError.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function saveProfile() {
  isSaving.value = true
  saveError.value = ''
  successMessage.value = ''
  fieldErrors.value = {}

  try {
    user.value = await updateCurrentUser({
      ...profileForm,
    })

    fillProfileForm(user.value)
    successMessage.value = 'Profile updated successfully.'
  } catch (error) {
    saveError.value = error.message
    fieldErrors.value = error.fieldErrors || {}
  } finally {
    isSaving.value = false
  }
}

function logout() {
  localStorage.removeItem('amtpilot_access_token')
  router.push('/login')
}

onMounted(loadProfile)
</script>

<template>
  <main class="dashboard-page">
    <section class="dashboard">
      <header>
        <div class="brand">AmtPilot</div>

        <button type="button" @click="logout">Log out</button>
      </header>

      <section class="welcome">
        <p class="eyebrow">Dashboard</p>
        <h1>
          {{ user ? `Welcome, ${user.email}` : 'Welcome to AmtPilot' }}
        </h1>
        <p>
          Keep your profile up to date so AmtPilot can show relevant administrative information.
        </p>
      </section>

      <p v-if="isLoading" class="status-message">Loading your profile...</p>

      <p v-else-if="loadError" class="status-message error" role="alert">
        {{ loadError }}
      </p>

      <section v-else-if="user" class="profile-card">
        <h2>Your profile</h2>

        <form class="profile-form" @submit.prevent="saveProfile">
          <label>
            <span>Email</span>
            <input :value="user.email" type="email" disabled />
          </label>

          <label>
            <span>Preferred language</span>
            <input v-model="profileForm.preferredLanguage" maxlength="10" placeholder="en" />
            <small v-if="fieldErrors.preferredLanguage">
              {{ fieldErrors.preferredLanguage }}
            </small>
          </label>

          <label>
            <span>City</span>
            <input v-model="profileForm.city" maxlength="120" placeholder="Dortmund" />
            <small v-if="fieldErrors.city">
              {{ fieldErrors.city }}
            </small>
          </label>

          <label>
            <span>Country of origin</span>
            <input
              v-model="profileForm.countryOfOrigin"
              maxlength="120"
              placeholder="Your country of origin"
            />
            <small v-if="fieldErrors.countryOfOrigin">
              {{ fieldErrors.countryOfOrigin }}
            </small>
          </label>

          <label>
            <span>User type</span>
            <input
              v-model="profileForm.userType"
              maxlength="40"
              placeholder="Student, employee, family..."
            />
            <small v-if="fieldErrors.userType">
              {{ fieldErrors.userType }}
            </small>
          </label>

          <label>
            <span>Timezone</span>
            <input v-model="profileForm.timezone" maxlength="60" placeholder="Europe/Berlin" />
            <small v-if="fieldErrors.timezone">
              {{ fieldErrors.timezone }}
            </small>
          </label>

          <p v-if="saveError" class="message error" role="alert">
            {{ saveError }}
          </p>

          <p v-if="successMessage" class="message success" role="status">
            {{ successMessage }}
          </p>

          <button class="save-button" type="submit" :disabled="isSaving">
            {{ isSaving ? 'Saving...' : 'Save profile' }}
          </button>
        </form>
      </section>
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

header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.brand {
  color: #2563eb;
  font-size: 20px;
  font-weight: 700;
}

button {
  padding: 10px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #ffffff;
  color: #374151;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

button:hover {
  background: #f9fafb;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.welcome,
.profile-card {
  margin-top: 24px;
  padding: 36px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

h1,
h2 {
  margin: 0;
}

h1 {
  font-size: 30px;
}

h2 {
  font-size: 20px;
}

.welcome p:last-child {
  max-width: 640px;
  margin: 12px 0 0;
  color: #6b7280;
  line-height: 1.6;
}

.profile-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  margin-top: 24px;
}

label {
  display: grid;
  gap: 8px;
  color: #374151;
  font-size: 14px;
  font-weight: 600;
}

input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #ffffff;
  font: inherit;
}

input:focus {
  border-color: #2563eb;
  outline: 3px solid rgb(37 99 235 / 12%);
}

input:disabled {
  background: #f3f4f6;
  color: #6b7280;
}

small {
  color: #b91c1c;
}

.status-message,
.message {
  padding: 14px;
  border-radius: 8px;
}

.status-message {
  margin-top: 24px;
  background: #ffffff;
  color: #6b7280;
}

.message {
  grid-column: 1 / -1;
  margin: 0;
}

.error {
  background: #fef2f2;
  color: #b91c1c;
}

.success {
  background: #ecfdf5;
  color: #047857;
}

.save-button {
  grid-column: 1 / -1;
  justify-self: start;
  border-color: #2563eb;
  background: #2563eb;
  color: #ffffff;
}

.save-button:hover {
  background: #1d4ed8;
}

@media (max-width: 600px) {
  .dashboard-page {
    padding: 16px;
  }

  header,
  .welcome,
  .profile-card {
    padding: 20px;
  }

  .profile-form {
    grid-template-columns: 1fr;
  }
}
</style>
