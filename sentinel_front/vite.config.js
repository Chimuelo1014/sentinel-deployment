import path from 'node:path'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import tsconfigPaths from 'vite-tsconfig-paths'
import svgr from 'vite-plugin-svgr';



export default defineConfig({
	plugins: [tsconfigPaths(), react(), svgr()],
	resolve: {
		alias: {
			'@': path.resolve(__dirname, './src'),
			'@assets': path.resolve(__dirname, './src/assets'),
			'@icons': path.resolve(__dirname, './src/assets/icons'),
			'@images': path.resolve(__dirname, './src/assets/images'),
			'@styles': path.resolve(__dirname, './src/assets/styles'),
			'@components': path.resolve(__dirname, './src/ui/components'),
			'@pages': path.resolve(__dirname, './src/ui/pages'),
			'@constants': path.resolve(__dirname, './src/constants'),
			'@helpers': path.resolve(__dirname, './src/helpers'),
			'@hooks': path.resolve(__dirname, './src/hooks'),
			'@i18n': path.resolve(__dirname, './src/i18n'),
			'@locales': path.resolve(__dirname, './src/i18n/locales'),
			'@router': path.resolve(__dirname, './src/router'),
			'@services': path.resolve(__dirname, './src/services'),
			'@store': path.resolve(__dirname, './src/store'),
			'@theme': path.resolve(__dirname, './src/theme'),
			'@utils': path.resolve(__dirname, './src/utils'),
		},
	},	
})
