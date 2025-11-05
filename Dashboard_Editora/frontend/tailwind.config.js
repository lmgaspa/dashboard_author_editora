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

      /* Cores do sistema + Angular */
      colors: {
        // Cores Angular
        angular: {
          DEFAULT: "#DD0031",
          dark: "#C3002F",
          light: "#FF1744",
        },
        // Cores do sistema existentes
        surface: {
          DEFAULT: "var(--surface)",
          2: "var(--surface-2)",
          muted: "var(--surface-muted)",
        },
        ink: {
          1: "var(--ink-1)",
          2: "var(--ink-2)",
          3: "var(--ink-3)",
        },
        brand: {
          DEFAULT: "var(--brand)",
          contrast: "var(--brand-contrast)",
          50: "#eef7ff",
          100: "#dbeefe",
          200: "#bfe2fd",
          300: "#93cdfa",
          400: "#60b3f6",
          500: "#2997f0",
          600: "#1b7fd4"
        },
        success: "var(--success)",
        warning: "var(--warning)",
        danger: "var(--danger)",
        border: {
          1: "var(--border-1)",
          2: "var(--border-2)",
        },
      },

      borderRadius: { 
        card: "var(--radius-card)",
        pop: "var(--radius-pop)",
      },
      boxShadow: {
        card: "var(--shadow-card)",
        pop: "var(--shadow-pop)",
        angular: "0 4px 14px 0 rgba(221, 0, 49, 0.39)",
        "angular-hover": "0 6px 20px 0 rgba(221, 0, 49, 0.5)",
      },

      /* escala MAIOR (com lineHeight) */
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

      maxWidth: { 
        "map-narrow": "760px",
        "content": "1200px",
      },

      width: {
        '70': '280px',
      },

      animation: {
        'spin-slow': 'spin 20s linear infinite',
        'float': 'float 20s ease-in-out infinite',
        'pulse-slow': 'pulse 8s ease-in-out infinite',
        'fade-in': 'fadeIn 0.8s ease-out',
        'fade-in-delay': 'fadeIn 1s ease-out 0.3s both',
        'countdown-pulse': 'countdownPulse 1s ease-in-out',
      },

      keyframes: {
        float: {
          '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
          '33%': { transform: 'translate(30px, -30px) scale(1.1)' },
          '66%': { transform: 'translate(-20px, 20px) scale(0.9)' },
        },
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        countdownPulse: {
          '0%, 100%': { transform: 'scale(1)', opacity: '1' },
          '50%': { transform: 'scale(1.2)', opacity: '0.8' },
        },
      },

      backgroundImage: {
        'angular-gradient': 'linear-gradient(135deg, #DD0031 0%, #C3002F 100%)',
        'hero-gradient': 'linear-gradient(135deg, #667eea 0%, #764ba2 50%, #1a1a2e 100%)',
        'text-gradient': 'linear-gradient(135deg, #ffffff 0%, #e0e0e0 100%)',
      },
    },
  },
  plugins: [],
};
