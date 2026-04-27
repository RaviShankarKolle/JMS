import { render, screen } from '@testing-library/react'
import LoadingSpinner from '../components/LoadingSpinner'
import ErrorMessage from '../components/ErrorMessage'

describe('UI states', () => {
  it('shows loading text', () => {
    render(<LoadingSpinner label="Loading data..." />)
    expect(screen.getByText(/loading data/i)).toBeInTheDocument()
  })

  it('shows error message text', () => {
    render(<ErrorMessage message="API failed" />)
    expect(screen.getByText(/api failed/i)).toBeInTheDocument()
  })
})
