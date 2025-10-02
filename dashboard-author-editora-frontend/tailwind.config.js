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

      /* mapeia tokens → classes utilitárias */
      colors: {
        surface:      "var(--surface)",
        surface2:     "var(--surface-2)",
        surfaceMuted: "var(--surface-muted)",
        ink: {
          1: "var(--ink-1)",
          2: "var(--ink-2)",
          3: "var(--ink-3)",
        },
        brand: {
          DEFAULT:  "var(--brand)",
          contrast: "var(--brand-contrast)",
          50:"#eef7ff",100:"#dbeefe",200:"#bfe2fd",300:"#93cdfa",400:"#60b3f6",500:"#2997f0",600:"#1b7fd4"
        },
        success: "var(--success)",
        warning: "var(--warning)",
        danger:  "var(--danger)",
        border: {
          1: "var(--border-1)",
          2: "var(--border-2)",
        },
      },

      borderRadius: { card: "var(--radius-card)" },
      boxShadow: {
        card: "var(--shadow-card)",
        pop:  "var(--shadow-pop)",
      },

      /* sua escala MAIOR (com lineHeight) */
      fontSize: {
        xs:  ["1.0625rem", { lineHeight: "1.45rem" }],
        sm:  ["1.1875rem", { lineHeight: "1.6rem"  }],
        base:["1.375rem",  { lineHeight: "1.9rem"  }],
        lg:  ["1.5rem",    { lineHeight: "2.1rem"  }],
        xl:  ["1.875rem",  { lineHeight: "2.3rem"  }],
        "2xl":["2.25rem",  { lineHeight: "2.6rem"  }],
        "3xl":["2.75rem",  { lineHeight: "3.1rem"  }],
        "4xl":["3.25rem",  { lineHeight: "3.6rem"  }],
      },

      maxWidth: { "map-narrow": "760px" },
    },
  },
  plugins: [],
};
