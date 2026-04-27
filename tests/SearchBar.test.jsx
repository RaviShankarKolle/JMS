import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import SearchBar from '../components/SearchBar'

const mockNavigate = jest.fn()

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}))

describe('SearchBar', () => {
  beforeEach(() => {
    mockNavigate.mockClear()
  })

  it('submits search query and navigates', () => {
    render(
      <MemoryRouter>
        <SearchBar />
      </MemoryRouter>,
    )

    fireEvent.change(screen.getByLabelText(/search movies/i), {
      target: { value: 'Batman' },
    })
    fireEvent.click(screen.getByRole('button', { name: /search/i }))

    expect(mockNavigate).toHaveBeenCalledWith('/search?q=Batman')
  })
})
