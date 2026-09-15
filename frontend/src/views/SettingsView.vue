<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppHeader from '@/components/AppHeader.vue'
import { setLocale, t, translateCode } from '@/i18n'
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
    if (['en', 'de'].includes(user.value.preferredLanguage)) {
      setLocale(user.value.preferredLanguage)
    }
    successMessage.value = t('settings.saved')
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
  <main class="settings-page">
    <section class="settings-layout">
      <AppHeader :email="user?.email" />

      <section class="settings-intro">
        <RouterLink class="back-link" to="/dashboard">
          {{ t('common.backToDashboard') }}
        </RouterLink>
        <p class="eyebrow">{{ t('settings.eyebrow') }}</p>
        <h1>{{ t('settings.title') }}</h1>
        <p>{{ t('settings.description') }}</p>
      </section>

      <p v-if="isLoading" class="status-message">{{ t('settings.loading') }}</p>

      <p v-else-if="loadError" class="status-message error" role="alert">
        {{ loadError }}
      </p>

      <template v-else-if="user">
        <section class="profile-card">
          <div class="profile-header">
            <div class="profile-identity">
              <div class="profile-avatar" aria-hidden="true">
                {{ user.email.charAt(0).toUpperCase() }}
              </div>

              <div>
                <p class="section-label">{{ t('settings.personalization') }}</p>
                <h2>{{ t('settings.profile') }}</h2>
                <p class="profile-description">
                  {{ t('settings.profileDescription') }}
                </p>
              </div>
            </div>

            <span class="role-badge">{{ translateCode('role', user.role, user.role) }}</span>
          </div>

          <div class="account-email">
            <div>
              <span>{{ t('settings.email') }}</span>
              <strong>{{ user.email }}</strong>
            </div>
            <small>{{ t('settings.emailHint') }}</small>
          </div>

          <form class="profile-form" @submit.prevent="saveProfile">
            <fieldset>
              <legend>{{ t('settings.aboutYou') }}</legend>
              <p class="fieldset-description">{{ t('settings.aboutDescription') }}</p>

              <div class="form-grid">
                <label for="profile-city">
                  <span>{{ t('settings.city') }}</span>
                  <input
                    id="profile-city"
                    v-model="profileForm.city"
                    autocomplete="address-level2"
                    maxlength="120"
                    placeholder="Dortmund"
                  />
                  <small v-if="fieldErrors.city" class="field-error">
                    {{ fieldErrors.city }}
                  </small>
                </label>

                <label for="profile-country">
                  <span>{{ t('settings.country') }}</span>
                  <input
                    id="profile-country"
                    v-model="profileForm.countryOfOrigin"
                    autocomplete="country-name"
                    maxlength="120"
                    :placeholder="t('settings.countryPlaceholder')"
                  />
                  <small v-if="fieldErrors.countryOfOrigin" class="field-error">
                    {{ fieldErrors.countryOfOrigin }}
                  </small>
                </label>

                <label for="profile-user-type" class="full-width">
                  <span>{{ t('settings.situation') }}</span>
                  <input
                    id="profile-user-type"
                    v-model="profileForm.userType"
                    list="user-type-options"
                    maxlength="40"
                    :placeholder="t('settings.situationPlaceholder')"
                  />
                  <datalist id="user-type-options">
                    <option value="International student">
                      {{ t('profile.internationalStudent') }}
                    </option>
                    <option value="Employee">{{ t('profile.employee') }}</option>
                    <option value="Job seeker">{{ t('profile.jobSeeker') }}</option>
                    <option value="Family member">{{ t('profile.familyMember') }}</option>
                  </datalist>
                  <small v-if="fieldErrors.userType" class="field-error">
                    {{ fieldErrors.userType }}
                  </small>
                </label>
              </div>
            </fieldset>

            <fieldset>
              <legend>{{ t('settings.preferences') }}</legend>
              <p class="fieldset-description">{{ t('settings.preferencesDescription') }}</p>

              <div class="form-grid">
                <label for="profile-language">
                  <span>{{ t('settings.preferredLanguage') }}</span>
                  <input
                    id="profile-language"
                    v-model="profileForm.preferredLanguage"
                    list="language-options"
                    maxlength="10"
                    placeholder="en"
                  />
                  <datalist id="language-options">
                    <option value="en">{{ t('language.english') }}</option>
                    <option value="de">{{ t('language.german') }}</option>
                  </datalist>
                  <small v-if="fieldErrors.preferredLanguage" class="field-error">
                    {{ fieldErrors.preferredLanguage }}
                  </small>
                </label>

                <label for="profile-timezone">
                  <span>{{ t('settings.timezone') }}</span>
                  <input
                    id="profile-timezone"
                    v-model="profileForm.timezone"
                    list="timezone-options"
                    maxlength="60"
                    placeholder="Europe/Berlin"
                  />
                  <datalist id="timezone-options">
                    <option value="Europe/Berlin"></option>
                  </datalist>
                  <small v-if="fieldErrors.timezone" class="field-error">
                    {{ fieldErrors.timezone }}
                  </small>
                </label>
              </div>
            </fieldset>

            <div class="form-footer">
              <div class="form-feedback">
                <p v-if="saveError" class="message error" role="alert">
                  {{ saveError }}
                </p>

                <p v-if="successMessage" class="message success" role="status">
                  {{ successMessage }}
                </p>
              </div>

              <button class="save-button" type="submit" :disabled="isSaving">
                {{ isSaving ? t('settings.saving') : t('settings.save') }}
              </button>
            </div>
          </form>
        </section>

        <section class="session-card">
          <div>
            <h2>{{ t('settings.session') }}</h2>
            <p>{{ t('settings.sessionDescription') }}</p>
          </div>

          <button class="logout-button" type="button" @click="logout">
            {{ t('settings.logout') }}
          </button>
        </section>
      </template>
    </section>
  </main>
</template>

<style scoped>
.settings-page {
  min-height: 100vh;
  padding: 24px;
  background: #f4f7fb;
  color: #1f2937;
}

.settings-layout {
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
}

.settings-intro,
.profile-card,
.session-card {
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

.eyebrow,
.section-label {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
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

.settings-intro > p:last-child,
.profile-description,
.session-card p {
  color: #6b7280;
  line-height: 1.5;
}

.settings-intro > p:last-child {
  margin: 10px 0 0;
}

.profile-header,
.profile-identity,
.account-email,
.form-footer,
.session-card {
  display: flex;
}

.profile-header,
.account-email,
.form-footer,
.session-card {
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.profile-header,
.profile-identity {
  align-items: flex-start;
}

.profile-identity {
  gap: 16px;
}

.profile-avatar {
  display: grid;
  flex: 0 0 48px;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 14px;
  background: #2563eb;
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
}

.profile-description {
  max-width: 560px;
  margin: 8px 0 0;
}

.role-badge {
  padding: 6px 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
}

.account-email {
  margin-top: 24px;
  padding: 16px 18px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f8fafc;
}

.account-email span,
.account-email strong {
  display: block;
}

.account-email span {
  margin-bottom: 4px;
  color: #6b7280;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.account-email strong {
  overflow-wrap: anywhere;
}

.account-email small {
  color: #6b7280;
  text-align: right;
}

.profile-form {
  display: grid;
  gap: 24px;
  margin-top: 24px;
}

fieldset {
  min-width: 0;
  margin: 0;
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
}

legend {
  padding: 0 8px;
  font-size: 16px;
  font-weight: 700;
}

.fieldset-description {
  margin: 0 0 18px;
  color: #6b7280;
  font-size: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.full-width {
  grid-column: 1 / -1;
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

.field-error {
  color: #b91c1c;
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

button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.save-button {
  flex: 0 0 auto;
  border-color: #2563eb;
  background: #2563eb;
  color: #ffffff;
}

.save-button:hover {
  background: #1d4ed8;
}

.form-feedback {
  flex: 1;
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

.session-card p {
  margin: 6px 0 0;
}

.logout-button {
  flex: 0 0 auto;
  border-color: #fecaca;
  color: #b91c1c;
}

.logout-button:hover {
  background: #fef2f2;
}

@media (max-width: 600px) {
  .settings-page {
    padding: 16px;
  }

  .settings-intro,
  .profile-card,
  .session-card {
    padding: 20px;
  }

  .profile-header,
  .account-email,
  .form-footer,
  .session-card {
    align-items: stretch;
    flex-direction: column;
  }

  .account-email small {
    text-align: left;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .full-width {
    grid-column: auto;
  }

  .save-button,
  .logout-button {
    width: 100%;
  }
}
</style>
