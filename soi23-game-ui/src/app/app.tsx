import { SnackbarProvider } from 'notistack'
import '@fontsource/public-sans'
import {
    CssVarsProvider,
    Stack,
    Typography,
    getInitColorSchemeScript
} from '@mui/joy'
import StompProvider from './stomp/StompProvider'
import theme from './theme'
import PlayField from './PlayField'

export default function App() {
    return (
        <StompProvider>
            {getInitColorSchemeScript({defaultMode:'dark'})}
            <SnackbarProvider preventDuplicate>
                <CssVarsProvider theme={theme} defaultMode='system'>
                    <Stack
                        spacing={2}
                        alignItems='center'
                        justifyContent='center'
                    >
                        <Typography
                            noWrap
                            level='display1'
                            variant='soft'
                            color='primary'
                        >
                            SOI Game
                        </Typography>
                        <PlayField />
                    </Stack>
                </CssVarsProvider>
            </SnackbarProvider>
        </StompProvider>
    )
}
