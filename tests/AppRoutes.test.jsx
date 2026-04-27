import { render, screen } from '@testing-library/react'
import { Provider } from 'react-redux'
import { MemoryRouter } from 'react-router-dom'
import App from '../App'
import { store } from '../store'

jest.mock('../services/movieApi', () => ({
  getTrendingMovies: jest.fn().mockResolvedValue([]),
  searchMoviesByTitle: jest.fn().mockResolvedValue([]),
  getMovieDetailsById: jest.fn().mockResolvedValue({
    id: 1,
    title: 'Sample',
    releaseDate: '2020-01-01',
    rating: 7,
    posterPath: '',
    overview: 'Sample overview',
    genres: [],
    runtime: 100,
  }),
}))

describe('App routes', () => {
  it('renders 404 page for invalid route', async () => {
    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={['/bad-route']}>
          <App />
        </MemoryRouter>
      </Provider>,
    )

    expect(await screen.findByText(/page not found/i)).toBeInTheDocument()
  })
})
