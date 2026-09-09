<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getCurrentUser } from '@/services/user'

const router = useRouter()

const user = ref(null)
const isLoading = ref(true)
const errorMessage = ref('')

async function loadProfile() {
  try {
    user.value = await getCurrentUser()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
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
        <p>View your profile and continue with your administrative applications.</p>
      </section>

      <p v-if="isLoading" class="status-message">Loading your profile...</p>

      <p v-else-if="errorMessage" class="status-message error" role="alert">
        {{ errorMessage }}
      </p>

      <section v-else-if="user" class="profile-card">
        <h2>Your profile</h2>

        <dl class="profile-grid">
          <div>
            <dt>Email</dt>
            <dd>{{ user.email }}</dd>
          </div>

          <div>
            <dt>City</dt>
            <dd>{{ user.city || 'Not provided' }}</dd>
          </div>

          <div>
            <dt>Preferred language</dt>
            <dd>{{ user.preferredLanguage }}</dd>
          </div>

          <div>
            <dt>Timezone</dt>
            <dd>{{ user.timezone }}</dd>
          </div>

          <div>
            <dt>Role</dt>
            <dd>{{ user.role }}</dd>
          </div>
        </dl>
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
  padding: 9px 14px;
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
  max-width: 600px;
  margin: 12px 0 0;
  color: #6b7280;
  line-height: 1.6;
}

.status-message {
  margin-top: 24px;
  padding: 18px;
  background: #ffffff;
  border-radius: 10px;
  color: #6b7280;
}

.error {
  background: #fef2f2;
  color: #b91c1c;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
  margin: 24px 0 0;
}

.profile-grid div {
  padding: 18px;
  background: #f9fafb;
  border-radius: 10px;
}

dt {
  margin-bottom: 6px;
  color: #6b7280;
  font-size: 13px;
  font-weight: 600;
}

dd {
  margin: 0;
  overflow-wrap: anywhere;
  font-weight: 600;
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

  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
