import React, {
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
    BALL_BASE_SVG_PROPS, PLAYER_HEIGHT, PLAYER_SPEED
} from '../../utils/const'
import Arena from '../../utils/Arena'
import useSubmit from './useSubmit'
import useStompLogic from './useStompLogic'
import useMessageHandler from './useMessageHandler'

interface BallProps extends React.SVGProps<SVGCircleElement> {
    style: React.CSSProperties,
}
interface LineProps extends React.SVGProps<SVGLineElement> {
    style: React.CSSProperties,
}

export default function usePlayField() {
    const [pendingGameId, setPendingGameId] = useState<string>('')
    const [pendingPlayerSpeed, setPlayerSpeed] = useState<number>(PLAYER_SPEED)
    const [pendingPlayerHeight, setPendingPlayerHeight] = useState<number>(PLAYER_HEIGHT)
    const [pendingPlayerId, setPendingPlayerId] = useState<string>('')
    const [token, setToken] = useState<string>('')
    const [teamsScore, setTeamsScore] = useState<TeamsScore | null>(null)
    const [ballAnimation, setBallAnimation] = useState<BallAnimation | null>(null)
    const [playerDTOMap, setPlayerDTOMap] = useState<PlayerDTOMap>({})

    const arenaRef = useRef<Arena>(new Arena())

    const ballProps: BallProps = useMemo(() => {
        const customStyle: React.CSSProperties = {}
        if (ballAnimation !== null) {
            document.documentElement.style.setProperty('--ball-end-y', `${ballAnimation.endY}px`)
            document.documentElement.style.setProperty('--ball-end-x', `${ballAnimation.endX}px`)
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

    const lineProps: LineProps = useMemo(() => {
        const customStyle1: React.CSSProperties = {}
        return {
            style: customStyle1,
            x1: ballAnimation?.startX,
            y1: ballAnimation?.startY,
            x2: ballAnimation?.endX,
            y2: ballAnimation?.endY,
            stroke: 'white',
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
            arenaRef.current.setPlayerPosition({ team: playerDTO.team, y: playerDTO.y})
            arenaRef.current.getPlayer()?.setPlayerHeight(playerDTO.playerHeight)
            arenaRef.current.getPlayer()?.setPlayerSpeed(playerDTO.playerSpeed)
        }
        /* TODO
        Set the new value of playerDTOMap
        */
        console.log('handlePlayerDTOChange '+ JSON.stringify(playerDTO))
        setPlayerDTOMap((oldPlayerDTOMap) => ({
            ...oldPlayerDTOMap,
            [playerDTO.id]: playerDTO,
        }))
    }, [playerId])
    const {
        handleMessageChange
    } = useMessageHandler()

    const handleChangeSpeed = useCallback((event: Event, value: number | number[], activeThumb: number) => {
        setPlayerSpeed(value as number)
    }, [])
    const handleChangeHeight = useCallback((event: Event, value: number | number[], activeThumb: number) => {
        setPendingPlayerHeight(value as number)
    }, [])

    const {
        sendStart,
        sendAnimationEnded,
        sendPosition,
        sendTeam,
        sendSettings,
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
    const handleSendSettings = useCallback(() => {
        /* TODO
        Send the settings to the backend
        */
        console.log('sendSettings '+ JSON.stringify({ 'playerSpeed': pendingPlayerSpeed, 'playerSize': pendingPlayerHeight,token}))
        sendSettings(JSON.stringify({ 'playerSpeed': pendingPlayerSpeed, 'playerSize': pendingPlayerHeight,token}))

    }, [sendSettings, pendingPlayerSpeed, pendingPlayerHeight, token])
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

    const handleTeamChange = useCallback((newvalue:string|null) => {
        // imposto il team scelto del giocatore
        sendTeam(JSON.stringify({playerId, 'team': newvalue==='left'?0:1,token}))
    }, [playerId, sendTeam, token])
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
        handleTeamChange,
        handleSendSettings,
        handleChangeSpeed,
        handleChangeHeight,
        lineProps,
    }
}
