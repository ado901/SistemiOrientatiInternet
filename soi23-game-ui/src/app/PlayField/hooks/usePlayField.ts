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

interface BallProps extends React.SVGProps<SVGCircleElement> {
    style: React.CSSProperties,
}

export default function usePlayField() {
    const [pendingGameId, setPendingGameId] = useState<string>('')
    const [pendingPlayerId, setPendingPlayerId] = useState<string>('')
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
        disableEdit: !!teamsScore,
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
        sendStart,
        sendAnimationEnded,
        sendPosition
    } = useStompLogic({
        gameId,
        playerId,
        onTeamsScoreChange: setTeamsScore,
        onBallAnimationChange: handleBallAnimationChange,
        onPlayerDTOChange: handlePlayerDTOChange,
    })

    const handleKeyDown = useCallback(({ key }: KeyboardEvent) => {
        /* TODO
        Map the key so that you can:
            - set the moving direction of the player
            - request the start of the game
        */
       const player=arenaRef.current.getPlayer()
       switch (key) {
        case 'w':
            player?.setDirection(PlayerDirection.Up)
            break
        case 's':
            player?.setDirection(PlayerDirection.Down)
            break
        case ' ':
            sendStart(JSON.stringify({"playerId": playerId}))
            break
       }
       
    }, [sendStart, playerId])

    const handleKeyUp = useCallback(({ key }: KeyboardEvent) => {
        /* TODO
        Map the key so that you can reset the moving direction
        of the player.
        Be aware that the user could have already pressed the key
        corresponding to the opposite player direction
        */
        const player=arenaRef.current.getPlayer()
        switch (key) {
            case 'w':
                if (player?.getDirection() === PlayerDirection.Up) {
                    player?.setDirection(PlayerDirection.Hold)
                }
                break
            case 's':
                if (player?.getDirection() === PlayerDirection.Down) {
                    player?.setDirection(PlayerDirection.Hold)
                }
                break
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
        sendPosition(JSON.stringify({"playerId": playerId, "y": playerPositionY}))
    }, [sendPosition, playerId])

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
        disableEdit: !!teamsScore,
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
