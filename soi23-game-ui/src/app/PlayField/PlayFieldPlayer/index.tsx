import { CSSProperties, useEffect, useMemo } from 'react'
import { enqueueSnackbar } from 'notistack'
import { Typography } from '@mui/joy'
import {
    LEFT_TEAM_X,
    PLAYER_NOT_READY_SVG_PROPS,
    PLAYER_READY_SVG_PROPS,
    PLAYER_TEXT_BASE_STYLE,
    PLAYER_TEXT_LEFT_TEAM_STYLE,
    PLAYER_TEXT_RIGHT_TEAM_STYLE,
    PLAYER_WIDTH,
    RIGHT_TEAM_X,
} from '../../utils/const'
import { PlayerDTO, PlayerTeam } from '../../utils/interfaces'

export default function PlayFieldPlayer({
    userId,
    playerId,
    player,
}: {
    userId?: string,
    playerId: string,
    player: PlayerDTO,
}) {
    const {
        team,
        y: posY,
        readyToStart,
        playerHeight,
        playerradius,
    } = player
    const svgprops: React.SVGProps<SVGRectElement> = useMemo(() => ({
        rx: playerradius,
        width: PLAYER_WIDTH,
        height: playerHeight,
    }), [playerHeight, playerradius])
    const isUser = useMemo(() => (
        userId === playerId
    ), [userId, playerId])

    const playerStyle: CSSProperties = useMemo(() => ({
        zIndex: isUser ? 10 : undefined,
    }), [isUser])

    const playerSvgProps: React.SVGProps<SVGRectElement> = useMemo(() => {
        const posX = team === PlayerTeam.LEFT
            ? LEFT_TEAM_X
            : RIGHT_TEAM_X
        const extraProps = readyToStart
            ? PLAYER_READY_SVG_PROPS
            : PLAYER_NOT_READY_SVG_PROPS

        return {
            ...svgprops,
            ...extraProps,
            x: posX - PLAYER_WIDTH / 2,
            y: posY - playerHeight / 2,
        }
    }, [team, posY, readyToStart, playerHeight, svgprops])

    const textSvgProps: React.SVGProps<SVGForeignObjectElement> = useMemo(() => ({
        x: team === PlayerTeam.LEFT
            ? LEFT_TEAM_X - 5 * PLAYER_WIDTH
            : RIGHT_TEAM_X + 1 * PLAYER_WIDTH,
        textAnchor: team === PlayerTeam.LEFT
            ? 'end'
            : 'start',
        overflow: 'visible',
        width: svgprops.width,
        height: svgprops.height,
    }), [team, svgprops	])

    const textStyle: React.CSSProperties = useMemo(() => {
        const sideStyle = team === PlayerTeam.LEFT
            ? PLAYER_TEXT_LEFT_TEAM_STYLE
            : PLAYER_TEXT_RIGHT_TEAM_STYLE
        return {
            ...PLAYER_TEXT_BASE_STYLE,
            ...sideStyle,
        }
    }, [team])

    useEffect(() => {
        if (isUser && !readyToStart) {
            enqueueSnackbar('Press Enter to start', { variant: 'info' })
        }
    }, [isUser, readyToStart])

    return (
        <g>
            <rect
                {...playerSvgProps}
                style={playerStyle}
            />
            <foreignObject
                {...textSvgProps}
                y={posY}
            >
                <Typography
                    noWrap
                    level='body3'
                    variant={isUser ? 'solid' : 'soft'}
                    style={textStyle}
                >
                    {playerId}
                </Typography>
            </foreignObject>
        </g>
    )
}
