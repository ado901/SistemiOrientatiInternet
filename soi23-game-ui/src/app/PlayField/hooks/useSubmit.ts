import { useCallback, useState } from 'react'

export default function useSubmit({
    pendingGameId,
    pendingPlayerId,
    disableEdit,
}: {
    pendingGameId: string,
    pendingPlayerId: string,
    disableEdit: boolean,
}) {
    const [gameId, setGameId] = useState<string>('')
    const [playerId, setPlayerId] = useState<string>('')

    const handleButtonClick = useCallback(() => {
        if (!disableEdit) {

            setGameId(pendingGameId)
            localStorage.setItem('gameId', pendingGameId)
            if (pendingPlayerId) {
                setPlayerId(pendingPlayerId)
                localStorage.setItem('playerId', pendingPlayerId)
            }
        }
    }, [pendingGameId, pendingPlayerId, disableEdit])

    return {
        gameId,
        playerId,
        handleButtonClick
    }
}
