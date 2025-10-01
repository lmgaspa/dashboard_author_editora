/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  darkMode: "class",
  theme: {
    extend: {
      fontFamily: {
        sans: [
          "Inter","ui-sans-serif","system-ui","-apple-system",
          "Segoe UI","Roboto","Helvetica Neue","Arial","Noto Sans",
          "Apple Color Emoji","Segoe UI Emoji"
        ],
        mono: ["ui-monospace","SFMono-Regular","Menlo","monospace"],
      },

      // sua brand mantida
      colors: {
        brand: { 50:"#eef7ff",100:"#dbeefe",200:"#bfe2fd",300:"#93cdfa",400:"#60b3f6",500:"#2997f0",600:"#1b7fd4" },
      },

      borderRadius: { card: "12px" },
      boxShadow: {
        card: "0 1px 2px rgba(16,24,40,.06), 0 1px 1px rgba(16,24,40,.04)",
      },

      // tipografia um passo acima
      fontSize: {
        xs:  ["0.82rem", { lineHeight: "1.1rem" }],
        sm:  ["0.95rem", { lineHeight: "1.35rem" }],
        base:["1.0625rem", { lineHeight: "1.55rem" }],     // ~17px
        lg:  ["1.1875rem", { lineHeight: "1.7rem" }],      // ~19px
        xl:  ["1.375rem", { lineHeight: "1.9rem" }],
        "2xl":["1.75rem", { lineHeight: "2.1rem" }],
        "3xl":["2.125rem", { lineHeight: "2.45rem" }],
      },

      // largura de container opcional (útil para o mapa estreito)
      maxWidth: {
        "map-narrow": "760px",
      },
    },
  },
  plugins: [],
};
