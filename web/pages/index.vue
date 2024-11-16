<template>
  <v-row
    justify="center"
    align="center"
  >
    <v-col
      cols="12"
      sm="8"
      md="6"
    >
      <v-card>
        <h2>Managers Status</h2>
        <div
          id="status-card"
          v-html="syntaxHighlight(status)"
        ></div>
      </v-card>
      <br />
      <v-card>
        <h2>Healthcheck</h2>
        <div
          id="status-card"
          v-html="syntaxHighlight(healthcheck)"
        ></div>
      </v-card>
    </v-col>
  </v-row>
</template>

<script>
import client from '../commons/client'

export default {
  name: 'IndexPage',
  data() {
    return { status: null, healthcheck: null }
  },
  mounted() {
    const that = this
    this.reload()
    setInterval(() => {
      that.reload()
    }, 5000)
  },
  methods: {
    syntaxHighlight(json) {
      if (typeof json != 'string') {
        json = JSON.stringify(json, undefined, 2)
      }
      json = json
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
      return json.replace(
        /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g,
        function (match) {
          var cls = 'number'
          if (/^"/.test(match)) {
            if (/:$/.test(match)) {
              cls = 'key'
            } else {
              cls = 'string'
            }
          } else if (/true|false/.test(match)) {
            cls = 'boolean'
          } else if (/null/.test(match)) {
            cls = 'null'
          }
          return '<span class="' + cls + '">' + match + '</span>'
        }
      )
    },
    reload() {
      const that = this

      client
        .get('/status')
        .then((response) => {
          that.status = response.data
        })
        .catch((e) => {
          console.error('Error getting status: ', e)
          that.healthcheck = e.response.data
        })

      client
        .get(`/healthcheck`)
        .then((response) => {
          that.healthcheck = response.data
        })
        .catch((e) => {
          console.error('Error getting healthcheck', e)
          that.healthcheck = e.response.data
        })
    },
  },
}
</script>

<style @scoped>
pre {
  outline: 1px solid #ccc;
  padding: 5px;
  margin: 5px;
}

.string {
  color: green;
}

.number {
  color: darkorange;
}

.boolean {
  color: blue;
}

.null {
  color: magenta;
}

.key {
  color: red;
}
</style>
