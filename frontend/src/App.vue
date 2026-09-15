<script setup>
import AppFooter from '@/components/AppFooter.vue'
</script>

<template>
  <RouterView />
  <AppFooter />
</template>

<style>
* {
  box-sizing: border-box;
}

html {
  font-family:
    Inter,
    ui-sans-serif,
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    sans-serif;
}

body {
  margin: 0;
}
</style>
