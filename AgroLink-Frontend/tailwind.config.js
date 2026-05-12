/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        agri: {
          50: '#f2faf0',
          100: '#e0f4d9',
          200: '#c2eab5',
          300: '#97db87',
          400: '#7ccf5f',
          500: '#58b33a',
          600: '#43902b',
          700: '#367025',
          800: '#2e5921',
          900: '#28491f',
        },
        sidebar: {
          bg: '#1a1e2c',
          hover: '#242838',
          active: '#2a2f42',
          text: '#8b92a5',
          'text-active': '#ffffff',
          border: '#262b3a',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      borderRadius: {
        '2xl': '16px',
        '3xl': '20px',
      },
      boxShadow: {
        'card': '0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.06)',
        'card-hover': '0 4px 12px rgba(0,0,0,0.08), 0 2px 4px rgba(0,0,0,0.04)',
        'sidebar': '2px 0 8px rgba(0,0,0,0.06)',
      },
    },
  },
  plugins: [],
}
