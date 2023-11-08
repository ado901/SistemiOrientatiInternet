import {
    ChangeEvent,
    KeyboardEvent,
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState
} from 'react'
import {
    BallAnimation,
    PlayerDTO,
    PlayerDTOMap,
    PlayerDirection,
    TeamsScore,
} from '../../utils/interfaces'
import {
    BALL_BASE_SVG_PROPS,
} from '../../utils/const'
import Arena from '../../utils/Arena'
import useSubmit from './useSubmit'
import useStompLogic from './useStompLogic'
import useMessageHandler from './useMessageHandler'

interface BallProps extends React.SVGProps<SVGCircleElement> {
    style: React.CSSProperties,
}

export default function usePlayField() {
    const [pendingGameId, setPendingGameId] = useState<string>('')
    const [pendingPlayerId, setPendingPlayerId] = useState<string>('')
    const [token, setToken] = useState<string>('')
    const [teamsScore, setTeamsScore] = useState<TeamsScore | null>(null)
    const [ballAnimation, setBallAnimation] = useState<BallAnimation | null>(null)
    const [playerDTOMap, setPlayerDTOMap] = useState<PlayerDTOMap>({})

    const arenaRef = useRef<Arena>(new Arena())

    const ballProps: BallProps = useMemo(() => {
        const customStyle: React.CSSProperties = {}
        if (ballAnimation !== null) {
            document.documentElement.style.setProperty('--ball-end-y', `${ballAnimation.endY}`)
            document.documentElement.style.setProperty('--ball-end-x', `${ballAnimation.endX}`)
            customStyle.animationName = 'ballAnimation'
            customStyle.animationTimingFunction = 'linear'
            customStyle.animationFillMode = 'forwards'
            customStyle.animationDuration = `${ballAnimation.time}s`
        } else {
            customStyle.visibility = 'hidden'
        }
        return {
            style: customStyle,
            ...BALL_BASE_SVG_PROPS,
            cx: ballAnimation?.startX,
            cy: ballAnimation?.startY,
        }
    }, [ballAnimation])

    const {
        gameId,
        playerId,
        handleButtonClick
    } = useSubmit({
        pendingGameId,
        pendingPlayerId,
        disableEdit: !!token,
    })

    const handleBallAnimationChange = useCallback((ballAnim: BallAnimation) => {
        setBallAnimation((oldBallAnim) => {
            if (oldBallAnim?.endX === ballAnim.endX && oldBallAnim?.endY === ballAnim.endY) {
                return oldBallAnim
            }
            window.requestAnimationFrame(
                () => setBallAnimation(ballAnim)
            )
            return null
        })
    }, [])

    const handlePlayerDTOChange = useCallback((playerDTO: PlayerDTO) => {
        if (playerId === playerDTO.id) {
            arenaRef.current.setPlayerPosition({ team: playerDTO.team, y: playerDTO.y })
        }
        /* TODO
        Set the new value of playerDTOMap
        */
        setPlayerDTOMap((oldPlayerDTOMap) => ({
            ...oldPlayerDTOMap,
            [playerDTO.id]: playerDTO,
        }))
    }, [playerId])
    const {
        handleMessageChange
    } = useMessageHandler()

    const {
        sendStart,
        sendAnimationEnded,
        sendPosition
    } = useStompLogic({
        gameId,
        playerId,
        playerToken: token,
        onTokenChange: setToken,
        onTeamsScoreChange: setTeamsScore,
        onBallAnimationChange: handleBallAnimationChange,
        onPlayerDTOChange: handlePlayerDTOChange,
        onMessageChange: handleMessageChange,
    })

    function mapPlayerKey(key: string): {
        direction: PlayerDirection
    } | null {
        switch (key) {
            case 'w':
                return {direction: PlayerDirection.Up }
            case 's':
                return { direction: PlayerDirection.Down }
            default:
                return null
        }
    }
    const handleKeyDown = useCallback(({ key }: KeyboardEvent) => {
        /* TODO
        Map the key so that you can:
            - set the moving direction of the player
            - request the start of the game
        */
        const mappedDir = mapPlayerKey(key)
        if (mappedDir) {
            arenaRef.current.getPlayer()?.setDirection(mappedDir.direction)
        }
        if (key === 'Enter') {
            sendStart(JSON.stringify({playerId,token}))
        }
    }, [sendStart, arenaRef, playerId, token])

    const handleKeyUp = useCallback(({ key }: KeyboardEvent) => {
        /* TODO
        Map the key so that you can reset the moving direction
        of the player.
        Be aware that the user could have already pressed the key
        corresponding to the opposite player direction
        */
        const mappedDir = mapPlayerKey(key)
        if (mappedDir) {
            if (mappedDir.direction === arenaRef.current.getPlayer()?.getDirection()) {
                arenaRef.current.getPlayer()?.setDirection(PlayerDirection.Hold)
            }
        }
    }, [])

    const handleAnimationEnd = useCallback(() => {
        /* TODO
        Notify the backend that the animation ended
        */
        sendAnimationEnded()
    }, [sendAnimationEnded])

    const handlePlayerPositionYChange = useCallback((playerPositionY: number) => {
        /* TODO
        Notify the backend the new player position
        */
        sendPosition(JSON.stringify({playerId, 'y': playerPositionY,token}))
    }, [sendPosition, playerId, token])

    const handleGameIdChange = useCallback((event: ChangeEvent<HTMLInputElement>) => {
        setPendingGameId(event.target.value)
    }, [])

    const handlePlayerIdChange = useCallback((event: ChangeEvent<HTMLInputElement>) => {
        setPendingPlayerId(event.target.value)
    }, [])

    useEffect(() => {
        arenaRef.current.getPlayer()?.setOnChangePositionY(handlePlayerPositionYChange)
    }, [handlePlayerPositionYChange, teamsScore])

    return {
        gameId: pendingGameId,
        playerId: pendingPlayerId,
        disableEdit: !!token,
        teamsScore,
        playerDTOMap,
        ballProps,
        handleKeyDown,
        handleKeyUp,
        handleAnimationEnd,
        handleGameIdChange,
        handlePlayerIdChange,
        handleButtonClick,
    }
}
