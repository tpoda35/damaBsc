import React, {useEffect, useRef, useState} from "react";
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls";
import "./Background.css";

const Background = () => {
    const mountRef = useRef(null);
    const [rotate, setRotate] = useState(true);

    const rotateRef = useRef(true);
    const tableRotationRef = useRef(0);

    useEffect(() => {
        rotateRef.current = rotate;
    }, [rotate]);

    useEffect(() => {
        const mount = mountRef.current;
        const width = mount.clientWidth;
        const height = mount.clientHeight;

        const renderer = new THREE.WebGLRenderer({ antialias: true });
        renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        renderer.setSize(width, height);
        renderer.outputEncoding = THREE.sRGBEncoding;
        renderer.shadowMap.enabled = true;
        renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        mount.appendChild(renderer.domElement);

        // Scene + Camera
        const scene = new THREE.Scene();
        scene.background = new THREE.Color(0xf7f7f7);

        const camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 100);
        camera.position.set(0, 12, 14);

        const controls = new OrbitControls(camera, renderer.domElement);
        controls.target.set(0, 0.5, 0);
        controls.enableDamping = true;
        controls.dampingFactor = 0.12;
        controls.maxPolarAngle = Math.PI / 2.1;
        controls.enablePan = false;
        controls.enableZoom = false;

        // Lights
        const ambient = new THREE.HemisphereLight(0xffffff, 0x444444, 0.7);
        scene.add(ambient);

        const dir = new THREE.DirectionalLight(0xffffff, 1);
        dir.position.set(5, 10, 7);
        dir.castShadow = true;
        dir.shadow.mapSize.set(2048, 2048);
        scene.add(dir);

        // Wooden table
        const tableGroup = new THREE.Group();

        const tabletop = new THREE.Mesh(
            new THREE.BoxGeometry(22, 1, 22),
            new THREE.MeshStandardMaterial({
                color: 0x5b3a29,
                roughness: 0.55,
                metalness: 0.05,
            })
        );
        tabletop.position.set(0, 0, 0);
        tabletop.castShadow = true;
        tabletop.receiveShadow = true;
        tableGroup.add(tabletop);

        const legGeom = new THREE.BoxGeometry(1, 10, 1);
        const legMat = new THREE.MeshStandardMaterial({
            color: 0x4a2f1e,
            roughness: 0.7,
        });

        const legPositions = [
            [10, -5, 10],
            [-10, -5, 10],
            [10, -5, -10],
            [-10, -5, -10],
        ];

        legPositions.forEach(([x, y, z]) => {
            const leg = new THREE.Mesh(legGeom, legMat);
            leg.position.set(x, y, z);
            leg.castShadow = true;
            leg.receiveShadow = true;
            tableGroup.add(leg);
        });

        scene.add(tableGroup);

        // Checkers board
        const boardGroup = new THREE.Group();
        const squareSize = 1.6;

        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                const isDark = (row + col) % 2 === 1;
                const square = new THREE.Mesh(
                    new THREE.BoxGeometry(squareSize, 0.08, squareSize),
                    new THREE.MeshStandardMaterial({
                        color: isDark ? 0x2b2b2b : 0xf2e9d6,
                        roughness: 0.7,
                    })
                );

                square.position.x = (col - 3.5) * squareSize;
                square.position.y = 0;
                square.position.z = (row - 3.5) * squareSize;
                square.receiveShadow = true;

                boardGroup.add(square);
            }
        }

        boardGroup.position.y = 0.5;
        tableGroup.add(boardGroup);

        // Pieces
        const piecesGroup = new THREE.Group();
        const pieceGeom = new THREE.CylinderGeometry(0.6, 0.6, 0.2, 40);

        const redMat = new THREE.MeshStandardMaterial({ color: 0xd9534f });
        const blackMat = new THREE.MeshStandardMaterial({ color: 0x111111 });

        function addPiece(x, z, color) {
            const base = new THREE.Mesh(pieceGeom, color === "red" ? redMat : blackMat);
            base.position.set(x, 0.65, z);
            base.castShadow = true;

            const grp = new THREE.Group();
            grp.add(base);
            piecesGroup.add(grp);
            return grp;
        }

        function squarePos(col, row) {
            return [(col - 3.5) * squareSize, (row - 3.5) * squareSize];
        }

        // Red pieces
        for (let row = 0; row < 3; row++) {
            for (let col = 0; col < 8; col++) {
                if ((row + col) % 2 === 1) {
                    const [x, z] = squarePos(col, row);
                    addPiece(x, z, "red");
                }
            }
        }

        // Black pieces
        for (let row = 5; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                if ((row + col) % 2 === 1) {
                    const [x, z] = squarePos(col, row);
                    addPiece(x, z, "black");
                }
            }
        }

        tableGroup.add(piecesGroup);

        // Animate
        let reqId;
        function animate() {
            if (rotateRef.current) {
                tableGroup.rotation.y += 0.002;
                tableRotationRef.current = tableGroup.rotation.y;
            } else {
                tableGroup.rotation.y = tableRotationRef.current;
            }

            controls.update();
            renderer.render(scene, camera);
            reqId = requestAnimationFrame(animate);
        }
        animate();

        // Resize
        function resize() {
            const w = mount.clientWidth;
            const h = mount.clientHeight;
            renderer.setSize(w, h);
            camera.aspect = w / h;
            camera.updateProjectionMatrix();
        }

        window.addEventListener("resize", resize);

        return () => {
            window.removeEventListener("resize", resize);
            cancelAnimationFrame(reqId);
            mount.removeChild(renderer.domElement);
        };
    }, []);

    return (
        <>
            <div ref={mountRef} className="scene-background" />

            <button
                onClick={() => setRotate(!rotate)}
                className="rotate-btn"
            >
                {rotate ? "Stop Rotating" : "Start Rotating"}
            </button>
        </>
    );
};

export default Background;
