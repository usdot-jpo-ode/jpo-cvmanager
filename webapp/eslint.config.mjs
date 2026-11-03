import eslint from '@eslint/js'
import tseslint from 'typescript-eslint'

export default tseslint.config(eslint.configs.recommended, tseslint.configs.recommended, {
  rules: {
    'no-restricted-imports': [
      'error',
      {
        paths: [
          {
            name: '@mui/icons-material',
            message: 'Import icons from @mui/icons-material/<Icon> to avoid loading the full icon library in tests.',
          },
        ],
      },
    ],
    '@typescript-eslint/no-explicit-any': 'off',
    'no-empty-pattern': 'off',
  },
})
