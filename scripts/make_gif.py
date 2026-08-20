#!/usr/bin/env python3
"""
Renders a recorded terminal session (plain text file) as an animated GIF —
the "proof of work" asset for project READMEs.

Session file format:
  - lines starting with "$ "   -> prompt lines (green, bold-ish)
  - lines starting with "#"    -> comments (cyan)
  - lines of "=" or "-"        -> separators (dim gray)
  - everything else            -> output (light blue for JSON, default otherwise)

Usage: python3 scripts/make_gif.py <session.txt> <out.gif> [title]
"""
import re
import sys

from PIL import Image, ImageDraw, ImageFont

W, H = 980, 560
PAD = 28
LINE_H = 24
MAX_ROWS = (H - 2 * PAD - 46) // LINE_H

BG = (13, 17, 23, 255)          # GitHub dark
TITLE_BAR = (22, 27, 34, 255)
PROMPT = (99, 255, 124, 255)    # green
COMMENT = (139, 200, 255, 255)  # cyan
OUTPUT = (230, 237, 243, 255)   # near-white
JSON = (163, 212, 255, 255)     # light blue
DIM = (110, 118, 129, 255)      # gray
CURSOR = (99, 255, 124, 255)

ANSI_RE = re.compile(r"\x1b\[[0-9;]*m")


def load_font(size):
    for path in [
        "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSansMono-Bold.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationMono-Regular.ttf",
        "/usr/share/fonts/TTF/DejaVuSansMono.ttf",
    ]:
        try:
            return ImageFont.truetype(path, size)
        except OSError:
            continue
    return ImageFont.load_default()


def clean(line):
    return ANSI_RE.sub("", line).replace("\t", "    ").rstrip()


def wrap(line, width=118):
    out = []
    while len(line) > width:
        cut = line.rfind(" ", 0, width)
        if cut < width // 2:
            cut = width
        out.append(line[:cut])
        line = "  " + line[cut:].lstrip()
    out.append(line)
    return out


def classify(line):
    if line.startswith("$ "):
        return PROMPT, True
    if line.startswith("#"):
        return COMMENT, True
    if set(line) <= {"=", "-", " "} and len(line.strip()) > 3:
        return DIM, False
    stripped = line.lstrip()
    if stripped.startswith("{") or stripped.startswith("[") or stripped.startswith("   ->"):
        return JSON, False
    if line.strip().isdigit() or line.strip().startswith("->"):
        return JSON, False
    return OUTPUT, False


def main():
    src, dst = sys.argv[1], sys.argv[2]
    title = sys.argv[3] if len(sys.argv) > 3 else "terminal — java700"

    lines = []
    for raw in open(src, encoding="utf-8"):
        for wrapped in wrap(clean(raw)):
            lines.append(wrapped)

    font = load_font(17)
    font_title = load_font(16)
    frames = []

    # one frame per revealed line, with a short pause on separators
    for visible in range(1, len(lines) + 1):
        img = Image.new("RGBA", (W, H), BG)
        draw = ImageDraw.Draw(img)

        # title bar
        draw.rectangle([0, 0, W, 46], fill=TITLE_BAR)
        for i, color in enumerate([(255, 95, 86), (255, 189, 46), (39, 201, 63)]):
            draw.ellipse([24 + i * 26, 16, 38 + i * 26, 30], fill=color)
        draw.text((92, 12), title, font=font_title, fill=DIM)

        # window body
        start = max(0, visible - MAX_ROWS)
        y = 58
        for line in lines[start:visible]:
            color, bold = classify(line)
            draw.text((PAD, y), line, font=font, fill=color)
            y += LINE_H

        # blinking cursor
        if visible % 2 == 0:
            cy = 58 + (min(visible, MAX_ROWS) - 1) * LINE_H
            draw.rectangle([PAD, cy + 4, PAD + 10, cy + 20], fill=CURSOR)

        frames.append(img.convert("P", palette=Image.ADAPTIVE))

    pause_frames = []
    last = frames[-1] if frames else None
    if last is not None:
        for _ in range(14):
            pause_frames.append(last)
    frames.extend(pause_frames)

    durations = []
    for line in lines:
        durations.append(90 if (set(line) <= {"=", "-", " "} and len(line.strip()) > 3) else 120)
    durations.extend([120] * 14)

    frames[0].save(
        dst,
        save_all=True,
        append_images=frames[1:],
        duration=durations,
        loop=0,
        optimize=True,
        disposal=2,
    )
    print(f"wrote {dst} ({len(frames)} frames)")


if __name__ == "__main__":
    main()
