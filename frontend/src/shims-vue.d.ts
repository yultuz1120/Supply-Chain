declare module '*.vue' {
  import Vue from 'vue';
  export default Vue;
}

declare module 'is-promise'

declare module '@/config.json'

declare module '*.mp3' {
  const source: string
  export default source
}
