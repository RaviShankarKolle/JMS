import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Provider } from 'react-redux'
import { MemoryRouter } from 'react-router-dom'
import { store } from '../store'
import MovieCard from '../components/MovieCard'

const movie = {
  id: 10,
  title: 'Interstellar',
  releaseDate: '2014-11-07',
  rating: 8.7,
  posterPath: '',
}

describe('MovieCard', () => {
  it('renders movie information', () => {
    render(
      <Provider store={store}>
        <MemoryRouter>
          <MovieCard movie={movie} />
        </MemoryRouter>
      </Provider>,
    )

    expect(screen.getByText('Interstellar')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /view details/i })).toBeInTheDocument()
  })

  it('toggles favorite state', async () => {
    const user = userEvent.setup()
    render(
      <Provider store={store}>
        <MemoryRouter>
          <MovieCard movie={movie} />
        </MemoryRouter>
      </Provider>,
    )

    await user.click(screen.getByRole('button', { name: /save/i }))
    expect(screen.getByRole('button', { name: /saved/i })).toBeInTheDocument()
  })
})
