<template>
  <v-app>
    <nuxt />
    <v-dialog v-model="dialog.active" max-width="600" id="log-modal">
      <v-card>
        <v-card-title class="text-h5">
          {{ dialog.title }}
        </v-card-title>
        <v-card-text>
          {{ dialog.body }}
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="green darken-1" text @click="dialog.active = false">
            OK
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-app>
</template>

<script>
export default {
  name: 'EmptyLayout',
  data() {
    return {
      severity: 'debug',
      severityColorCSS: 'background-color: gray',
      dialog: {
        active: false,
        title: "",
        body: ""
      }
    }
  },
  mounted() {
    const modal = document.getElementById("log-modal")

    modal.open = function (title, text) {
      this.dialog.title = title
      this.dialog.body = text
      this.dialog.active = true
    }

    modal.close = function () {
      this.dialog.active = false
    }

    modal.close = modal.close.bind(this)
    modal.open = modal.open.bind(this)

  }
}
</script>

<style>
html,
body,
#__nuxt,
#__layout,
.main-container {
  height: 120%;
}

.main-content {
  /* background-image: url("~assets/logo.png");
  background-size: 8%; */
  background: none;
  background-color: green;
  min-height: 100%;
}
</style>
