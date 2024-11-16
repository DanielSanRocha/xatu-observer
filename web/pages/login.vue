<template>
  <section class="section">
    <div class="hero-body">
      <div class="container has-text-centered">
        <div class="column is-6 is-offset-3">
          <div class="box">
            <form @submit.prevent="submit">
              <div class="field">
                <p class="control has-icons-left has-icons-right">
                  <input v-model="email" class="input is-medium" type="email" placeholder="Email">
                  <span class="icon is-medium is-left">
                    <fa icon="envelope" transform="shrink-6" />
                  </span>
                </p>
              </div>
              <div class="field">
                <p class="control has-icons-left">
                  <input v-model="password" class="input is-medium" type="password" placeholder="Password">
                  <span class="icon is-small is-left">
                    <fa icon="lock" transform="shrink-6" />
                  </span>
                </p>
              </div>
              <button class="button is-block is-info is-large is-fullwidth">
                Login
              </button><br>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import client from '../commons/client'

export default {
  name: 'LoginPage',
  layout: 'empty',
  data() {
    return { email: '', password: '' }
  },
  mounted() {
    this.logModal = document.getElementById('log-modal')
  },
  methods: {
    submit(e) {
      const that = this

      client.post('/login', {
        email: this.email,
        password: this.password
      }).then((response) => {
        console.log(response)
        const token = response.data.token
        localStorage.setItem('token', token)

        const getUrl = window.location
        window.location.href = getUrl.protocol + '//' + getUrl.host
      }).catch((e) => {
        console.error(e)
        const message = e.response.data.message
        that.logModal.open(e.name, message)
      })
    }
  }
}
</script>
