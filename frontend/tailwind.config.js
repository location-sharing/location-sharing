/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'theme-bg-1': '#F9F7F7',
        'theme-bg-2': '#DBE2EF',
        'theme-ac-1': '#3F72AF',
        'theme-ac-2': '#112D4E',
      }
    },
  },
  plugins: [],
}

