import { useCallback } from 'react'
import { useSnackbar, VariantType } from 'notistack'
import { Message, MessageType } from '../../utils/interfaces'

function mapTypeToVariant(type: MessageType) {
    let variant: VariantType
    switch (type) {
        case MessageType.ERROR:
            variant = 'error'
            break
        case MessageType.WARNING:
            variant = 'warning'
            break
        default:
            variant = 'info'
    }
    return variant
}

function mapCode(code: string) {
    let output: string
    switch (code) {
        case 'GAME_NOT_FOUND':
            output = 'Game not found'
            break
        case 'PLAYER_NOT_FOUND':
            output = 'Player not found'
            break
        case 'PLAYER_ID_ALREADY_USED':
            output = 'Player ID already used'
            break
        case 'INVALID_PLAYER_TOKEN':
            output = 'Invalid Player token'
            break
        case 'POINT_SCORED':
            output = 'Point!'
            break
        case 'GAME_STARTED':
            output = 'Game started'
            break
        case 'NEW_PLAYER_JOINED':
            output = 'New player joined'
            break
        default:
            output = code
    }
    return output
}

export default function useMessageHandler() {
    const {
        enqueueSnackbar
    } = useSnackbar()

    const handleMessageChange = useCallback((message: Message) => {
        const variant = mapTypeToVariant(message.type)
        const msg = mapCode(message.code)
        enqueueSnackbar(msg, { variant })
    }, [enqueueSnackbar])

    return {
        handleMessageChange
    }
}
