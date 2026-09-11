<script setup>
import { ref, watch } from 'vue'
import { getAuthorities } from '@/services/authority'

const props = defineProps({
  city: {
    type: String,
    default: 'Dortmund',
  },
})

const authorities = ref([])
const isLoading = ref(false)
const errorMessage = ref('')

async function loadAuthorities() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    authorities.value = await getAuthorities(props.city || 'Dortmund')
  } catch (error) {
    authorities.value = []
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

watch(
  () => props.city,
  () => loadAuthorities(),
  { immediate: true },
)
</script>

<template>
  <section class="authority-card">
    <h2>Authorities in {{ city || 'Dortmund' }}</h2>

    <p v-if="isLoading">Loading authorities...</p>
    <p v-else-if="errorMessage" class="error">{{ errorMessage }}</p>

    <p v-else-if="authorities.length === 0">No authorities were found for this city.</p>

    <ul v-else class="authority-list">
      <li v-for="authority in authorities" :key="authority.id">
        <div>
          <h3>{{ authority.name }}</h3>
          <p>{{ authority.authorityType }}</p>
        </div>

        <a
          v-if="authority.officialUrl"
          :href="authority.officialUrl"
          target="_blank"
          rel="noopener noreferrer"
        >
          Official website
        </a>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.authority-card {
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  background: white;
}

.authority-card h2 {
  margin-top: 0;
}

.authority-list {
  display: grid;
  gap: 0.75rem;
  padding: 0;
  list-style: none;
}

.authority-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
}

.authority-list h3 {
  margin: 0 0 0.25rem;
}

.authority-list p {
  margin: 0;
  color: #64748b;
}

.authority-list a {
  color: #2563eb;
  font-weight: 600;
  text-decoration: none;
}

.error {
  color: #b91c1c;
}

@media (max-width: 600px) {
  .authority-list li {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
