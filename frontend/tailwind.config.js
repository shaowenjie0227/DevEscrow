/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: '#FFD100',
          deep: '#F5C400',
          soft: '#FFF0A6',
          ink: '#111111',
          canvas: '#F5F7FA'
        }
      },
      fontFamily: {
        display: ['"Outfit"', '"PingFang SC"', '"Microsoft YaHei"', 'sans-serif'],
        body: ['"Plus Jakarta Sans"', '"PingFang SC"', '"Microsoft YaHei"', 'sans-serif']
      },
      boxShadow: {
        float: '0 18px 40px rgba(17, 17, 17, 0.08)',
        glow: '0 22px 60px rgba(255, 209, 0, 0.28)'
      },
      keyframes: {
        breathe: {
          '0%, 100%': { transform: 'translateY(0px)', opacity: '1' },
          '50%': { transform: 'translateY(-6px)', opacity: '0.95' }
        },
        drift: {
          '0%': { transform: 'translate3d(0, 0, 0)' },
          '50%': { transform: 'translate3d(10px, -10px, 0)' },
          '100%': { transform: 'translate3d(0, 0, 0)' }
        },
        fadeUp: {
          '0%': { opacity: '0', transform: 'translateY(16px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        }
      },
      animation: {
        breathe: 'breathe 6s ease-in-out infinite',
        drift: 'drift 8s ease-in-out infinite',
        'fade-up': 'fadeUp 0.6s ease forwards'
      },
      backgroundImage: {
        'hero-grid':
          'linear-gradient(rgba(17, 17, 17, 0.06) 1px, transparent 1px), linear-gradient(90deg, rgba(17, 17, 17, 0.06) 1px, transparent 1px)'
      }
    }
  },
  plugins: []
}
