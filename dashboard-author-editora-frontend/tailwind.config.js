/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  "#eef7ff",
          100: "#dbeefe",
          200: "#bfe2fd",
          300: "#93cdfa",
          400: "#60b3f6",
          500: "#2997f0",   // azul principal
          600: "#1b7fd4",
        },
      },
      borderRadius: { card: "12px" }
    },
  },
  plugins: [],
};
