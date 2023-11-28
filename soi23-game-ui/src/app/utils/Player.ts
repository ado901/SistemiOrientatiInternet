import {
    PLAYFIELD_HEIGHT,
    PLAYER_HEIGHT,
    PLAYER_STEP,
    PLAYER_WIDTH,
    PLAYER_SPEED,
    FPS,
} from './const'
import { PlayerDirection, PlayerPosition } from './interfaces'

export default class Player {
    private direction: PlayerDirection
    private playerheight: number
    private playerradius: number
    private playerSpeed: number
    private playerStep: number
    private player_svg_props: React.SVGProps<SVGRectElement>
    constructor(
        private position: PlayerPosition,
        private onChangePositionY?: (playerPositionY: number) => void
    ) {
        this.direction = PlayerDirection.Hold
        this.playerheight = PLAYER_HEIGHT
        this.playerradius = Math.min(PLAYER_WIDTH, this.playerheight) / 2
        this.playerSpeed = PLAYER_SPEED
        this.playerStep = PLAYER_STEP
        this.player_svg_props = Object.freeze({
            rx: this.playerradius,
            width: PLAYER_WIDTH,
            height: this.playerheight,
        })
    }

    public getPosition() {
        return this.position
    }

    public setPosition(position: PlayerPosition) {
        this.position = position
    }
    public setPlayerSpeed(playerSpeed: number) {
        this.playerSpeed = playerSpeed
        this.playerStep = Math.floor(this.playerSpeed / FPS)
    }

    public getDirection() {
        return this.direction
    }

    public setDirection(direction: PlayerDirection) {
        this.direction = direction
    }

    public setOnChangePositionY(onChangePositionY: (playerPositionY: number) => void) {
        this.onChangePositionY = onChangePositionY
    }
    public setPlayerHeight(playerheight: number) {
        this.playerheight = playerheight
        this.playerradius = Math.min(PLAYER_WIDTH, this.playerheight) / 2
    }

    public animate() {
        const y = this.position.y

        let newY = y

        switch (this.direction) {
            case PlayerDirection.Up:
                newY = Math.max(this.playerheight / 2, y - this.playerStep)
                break
            case PlayerDirection.Down:
                newY = Math.min(PLAYFIELD_HEIGHT - this.playerheight / 2, y + this.playerStep)
                break
            default:
                return
        }

        if (newY !== this.position.y) {
            this.position.y = newY
            if (this.onChangePositionY) {
                this.onChangePositionY(this.position.y)
            }
        }
    }
}
